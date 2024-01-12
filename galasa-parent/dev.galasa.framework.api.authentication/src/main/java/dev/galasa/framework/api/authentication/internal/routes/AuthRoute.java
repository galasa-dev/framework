package dev.galasa.framework.api.authentication.internal.routes;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

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

    public AuthRoute(ResponseBuilder responseBuilder, String path, OidcProvider oidcProvider) {
        // Regex to match endpoint /auth and /auth/
        super(responseBuilder, "\\/?");
        this.oidcProvider = oidcProvider;
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
        if (requestBodyJson == null || !requestBodyJson.isValid()) {
            ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        try {
            // Send a POST request to Dex's /token endpoint
            JsonObject tokenResponseBodyJson = sendTokenPost(request, requestBodyJson);

            // Return the JWT and refresh token as the servlet's response
            String idTokenKey = "id_token";
            String refreshTokenKey = "refresh_token";
            if (tokenResponseBodyJson.has(idTokenKey) && tokenResponseBodyJson.has(refreshTokenKey)) {
                logger.info("Bearer and refresh tokens successfully received from issuer.");

                JsonObject responseJson = new JsonObject();
                responseJson.add("jwt", tokenResponseBodyJson.get(idTokenKey));
                responseJson.add(refreshTokenKey, tokenResponseBodyJson.get(refreshTokenKey));

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
        String secret = requestBodyJson.getSecret();
        String decodedSecret = new String(Base64.getDecoder().decode(secret));

        // Refresh tokens and authorization codes can be used in exchange for JWTs,
        // so we need to find out what method was used
        HttpResponse<String> tokenResponse = null;
        if (requestBodyJson.getRefreshToken() != null) {
            tokenResponse = oidcProvider.sendTokenPost(requestBodyJson.getClientId(), decodedSecret, requestBodyJson.getRefreshToken());
        } else {
            tokenResponse = oidcProvider.sendTokenPost(requestBodyJson.getClientId(), decodedSecret, requestBodyJson.getCode(), AuthCallbackRoute.getExternalAuthCallbackUrl());
        }
        return gson.fromJson(tokenResponse.body(), JsonObject.class);
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
}
