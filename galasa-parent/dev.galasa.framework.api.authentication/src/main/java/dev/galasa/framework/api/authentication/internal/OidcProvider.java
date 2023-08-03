/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

/**
 * A class that handles communications with an OpenID Connect (OIDC) Provider.
 */
public class OidcProvider {

    private final Log logger = LogFactory.getLog(getClass());

    private static final Gson gson = GalasaGsonBuilder.build();

    private String issuerUrl;

    private HttpClient httpClient = HttpClient.newHttpClient();

    public OidcProvider(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    /**
     * Sends a POST request with a given request body to an OpenID Connect /token endpoint, returning the received response.
     */
    public HttpResponse<String> sendTokenPost(JsonObject requestBody) throws IOException, InterruptedException {

        StringBuilder sbRequestBody = new StringBuilder();
        String clientId = requestBody.get("client_id").getAsString();
        String secret = requestBody.get("secret").getAsString();
        String refreshToken = requestBody.get("refresh_token").getAsString();

        // Convert the request's JSON object to the application/x-www-form-urlencoded content type
        // as required by the /token endpoint.
        sbRequestBody.append("grant_type=refresh_token");
        sbRequestBody.append("&client_id=" + clientId);
        sbRequestBody.append("&client_secret=" + secret);
        sbRequestBody.append("&refresh_token=" + refreshToken);

        // Create a POST request to the /token endpoint
        HttpRequest postRequest = HttpRequest.newBuilder()
            .POST(BodyPublishers.ofString(sbRequestBody.toString()))
            .header("Content-type", "application/x-www-form-urlencoded")
            .uri(URI.create(issuerUrl + "/token"))
            .build();

        HttpResponse<String> response = httpClient.send(postRequest, BodyHandlers.ofString());
        return response;
    }

    /**
     * Gets a JSON array of the JSON Web Keys (JWKs) from a GET request to an issuer's /keys endpoint
     */
    public JsonArray getJsonWebKeysFromIssuer(String issuer) throws InternalServletException, IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
            .GET()
            .header("Accept", "application/json")
            .uri(URI.create(issuer + "/keys"))
            .build();

        // Send a GET request to the issuer's /keys endpoint
        HttpResponse<String> response = httpClient.send(getRequest, BodyHandlers.ofString());

        JsonObject responseBodyJson = gson.fromJson(response.body(), JsonObject.class);
        if (!responseBodyJson.has("keys")) {
            logger.error("Error: No JSON Web Keys were found at the '" + issuer + "/keys' endpoint.");
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return responseBodyJson.get("keys").getAsJsonArray();
    }

    /**
     * Gets a JSON Web Key with a given key ID ('kid') from an OpenID connect issuer's /keys endpoint, returned as a JSON object
     */
    public JsonObject getJsonWebKeyFromIssuerByKeyId(String keyId, String issuer) throws IOException, InterruptedException, InternalServletException {
        JsonArray jsonWebKeys = getJsonWebKeysFromIssuer(issuer);

        // Iterate over the JSON array of JWKs, finding the one that matches the given key ID
        JsonObject matchingKey = null;
        for (JsonElement key : jsonWebKeys) {
            JsonObject keyAsJsonObject = key.getAsJsonObject();
            if (keyAsJsonObject.get("kid").getAsString().equals(keyId)) {
                matchingKey = keyAsJsonObject;
                break;
            }
        }
        return matchingKey;
    }

    public String getIssuer() {
        return this.issuerUrl;
    }
}
