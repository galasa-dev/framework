/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

/**
 * Authentication Servlet that acts as a proxy to send requests to Dex's /token
 * endpoint, returning the JWT received back from Dex.
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/auth" }, name = "Galasa Authentication")
public class AuthenticationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Gson gson = GalasaGsonBuilder.build();

    private Log logger = LogFactory.getLog(getClass());

    private List<String> requiredPayload = Arrays.asList("client_id", "secret", "refresh_token");

    protected Environment env = new SystemEnvironment();

    protected OidcProvider oidcProvider;

    @Override
    public void init() throws ServletException {
        oidcProvider = new OidcProvider(env.getenv("GALASA_DEX_ISSUER"));
        logger.info("Galasa Authentication API initialised");
    }


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String errorString = "";
        int httpStatusCode = HttpServletResponse.SC_OK;
        ResponseBuilder responseBuilder = new ResponseBuilder();

        try {
            JsonObject requestBodyJson = getRequestBodyAsJson(req);

            // Check that the request body contains the required payload
            if (requestBodyJson == null || !(requestBodyJson.keySet().containsAll(requiredPayload))) {
                ServletError error = new ServletError(GAL5400_BAD_REQUEST, req.getPathInfo());
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }

            // Send a POST request to Dex's /token endpoint and ensure the returned response contains a JWT.
            HttpResponse<String> tokenResponse = oidcProvider.sendTokenPost(requestBodyJson);

            JsonObject tokenResponseBodyJson = gson.fromJson(tokenResponse.body(), JsonObject.class);
            if (!tokenResponseBodyJson.has("id_token")) {
                ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            // Return the JWT as the servlet's response.
            String jwtJsonStr = "{\"jwt\": \"" + tokenResponseBodyJson.get("id_token").getAsString() + "\"}";
            responseBuilder.buildResponse(resp, "application/json", jwtJsonStr, httpStatusCode);
            return;

        } catch (InternalServletException ex) {
            // The message is a curated servlet message, we intentionally threw up to this level.
            errorString = ex.getMessage();
            httpStatusCode = ex.getHttpFailureCode();
            logger.error(errorString, ex);
        } catch (Exception e) {
            // We didn't expect this failure to arrive. So deliver a generic error message.
            errorString = new ServletError(GAL5000_GENERIC_API_ERROR).toString();
            httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            logger.error(errorString, e);
        }

        responseBuilder.buildResponse(resp, "application/json", errorString, httpStatusCode);
    }

    /**
     * Gets a given HTTP request's body as a JSON object.
     */
    private JsonObject getRequestBodyAsJson(HttpServletRequest request) throws IOException {
        StringBuilder sbRequestBody = new StringBuilder();
        BufferedReader bodyReader = request.getReader();

        String line = bodyReader.readLine();
        while (line != null) {
            sbRequestBody.append(line);
            line = bodyReader.readLine();
        }

        return gson.fromJson(sbRequestBody.toString(), JsonObject.class);
    }
}
