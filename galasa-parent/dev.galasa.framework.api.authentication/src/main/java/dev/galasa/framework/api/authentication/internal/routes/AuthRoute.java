package dev.galasa.framework.api.authentication.internal.routes;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.coreos.dex.api.DexOuterClass.Client;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.internal.TokenPayload;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class AuthRoute extends BaseRoute {

    private OidcProvider oidcProvider;
    private DexGrpcClient dexGrpcClient;

    private static final String ID_TOKEN_KEY      = "id_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    public AuthRoute(ResponseBuilder responseBuilder, String path, OidcProvider oidcProvider, DexGrpcClient dexGrpcClient) {
        // Regex to match endpoint /auth and /auth/
        super(responseBuilder, "\\/?");
        this.oidcProvider = oidcProvider;
        this.dexGrpcClient = dexGrpcClient;
    }

    /**
     * Sending a GET request to /auth redirects to the OpenID Connect provider's
     * authorization endpoint to authenticate a user.
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {

        logger.info("AuthRoute: handleGetRequest() entered.");
        HttpSession session = request.getSession(true);
        try {
            String clientId = queryParams.getSingleString("client_id", null);
            String clientCallbackUrl = queryParams.getSingleString("callback_url", null);

            // Make sure the required query parameters exist
            if (clientId == null || clientCallbackUrl == null || !isUrlValid(clientCallbackUrl)) {
                ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }

            // Store the callback URL in the session to redirect to at the end of the authentication process
            session.setAttribute("callbackUrl", clientCallbackUrl);

            String authUrl = oidcProvider.getConnectorRedirectUrl(clientId, AuthCallbackRoute.getExternalAuthCallbackUrl(), session);
            if (authUrl != null) {
                logger.info("Redirect URL to upstream connector received: " + authUrl);

                response.addHeader("Location", authUrl);
                return getResponseBuilder().buildResponse(response, null, null,
                        HttpServletResponse.SC_FOUND);
            } else {
                logger.info("Unable to get URL to redirect to upstream connector.");
            }

        } catch (InterruptedException e) {
            logger.error("GET request to the OpenID Connect provider's authorization endpoint was interrupted.", e);
        }

        session.invalidate();
        ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
        throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Sending a POST request to /auth issues a new bearer token using the provided
     * client ID, client secret, and refresh token.
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {

        logger.info("AuthRoute: handlePostRequest() entered.");

        // Check that the request body contains the required payload
        TokenPayload requestBodyJson = getRequestBodyAsJson(request);
        if (requestBodyJson == null || !isTokenPayloadValid(requestBodyJson)) {
            ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        try {
            // Send a POST request to Dex's /token endpoint
            JsonObject tokenResponseBodyJson = sendTokenPost(request, requestBodyJson);

            // Return the JWT and refresh token as the servlet's response
            if (tokenResponseBodyJson != null && tokenResponseBodyJson.has(ID_TOKEN_KEY) && tokenResponseBodyJson.has(REFRESH_TOKEN_KEY)) {
                logger.info("Bearer and refresh tokens successfully received from issuer.");

                JsonObject responseJson = new JsonObject();
                responseJson.add("jwt", tokenResponseBodyJson.get(ID_TOKEN_KEY));
                responseJson.add(REFRESH_TOKEN_KEY, tokenResponseBodyJson.get(REFRESH_TOKEN_KEY));

                return getResponseBuilder().buildResponse(response, "application/json", gson.toJson(responseJson),
                        HttpServletResponse.SC_OK);
            } else {
                logger.info("Unable to get new bearer and refresh tokens from issuer.");
            }

        } catch (InterruptedException e) {
            logger.error("POST request to the OpenID Connect provider's token endpoint was interrupted.", e);
        }

        ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
        throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Gets a given HTTP request's body as a JSON object.
     */
    private TokenPayload getRequestBodyAsJson(HttpServletRequest request) throws IOException {
        StringBuilder sbRequestBody = new StringBuilder();
        BufferedReader bodyReader = request.getReader();

        String line = bodyReader.readLine();
        while (line != null) {
            sbRequestBody.append(line);
            line = bodyReader.readLine();
        }

        return gson.fromJson(sbRequestBody.toString(), TokenPayload.class);
    }

    /**
     * Sends a POST request to the JWT issuer's /token endpoint and returns the
     * response's body as a JSON object.
     *
     * @param requestBodyJson the request payload containing the required parameters
     *                        for the /token endpoint
     */
    private JsonObject sendTokenPost(HttpServletRequest request, TokenPayload requestBodyJson)
            throws IOException, InterruptedException {
        String refreshToken = requestBodyJson.getRefreshToken();
        String clientId = requestBodyJson.getClientId();
        Client dexClient = dexGrpcClient.getClient(clientId);

        JsonObject response = null;
        if (dexClient != null) {
            String clientSecret = dexClient.getSecret();

            // Refresh tokens and authorization codes can be used in exchange for JWTs.
            // At this point, we either have a refresh token or an authorization code,
            // so perform the relevant POST request
            HttpResponse<String> tokenResponse = null;
            if (refreshToken != null) {
                tokenResponse = oidcProvider.sendTokenPost(clientId, clientSecret, refreshToken);
            } else {
                tokenResponse = oidcProvider.sendTokenPost(clientId, clientSecret, requestBodyJson.getCode(), AuthCallbackRoute.getExternalAuthCallbackUrl());
            }

            if (tokenResponse != null) {
                response = gson.fromJson(tokenResponse.body(), JsonObject.class);
            }
        }
        return response;
    }

    /**
     * Checks if a given URL is a valid URL.
     */
    private boolean isUrlValid(String url) {
        boolean isValid = false;
        try {
            new URL(url).toURI();
            isValid = true;
            logger.info("Valid URL provided: '" + url + "'");
        } catch (URISyntaxException | MalformedURLException e) {
            logger.error("Invalid URL provided: '" + url + "'");
        }
        return isValid;
    }

    /**
     * Checks if the POST request payload to pass to Dex's /token endpoint contains a client ID and either
     * a refresh token or an authorization code.
     */
    private boolean isTokenPayloadValid(TokenPayload requestPayload) {
        return (requestPayload.getClientId() != null) && (requestPayload.getRefreshToken() != null || requestPayload.getCode() != null);
    }
}
