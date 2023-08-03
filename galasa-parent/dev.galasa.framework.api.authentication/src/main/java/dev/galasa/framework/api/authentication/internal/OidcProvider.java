/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/**
 * A class that handles communications with an OpenID Connect (OIDC) Provider.
 */
public class OidcProvider {

    private final Log logger = LogFactory.getLog(getClass());

    private static final Gson gson = GalasaGsonBuilder.build();

    private String issuerUrl;

    protected HttpClient httpClient = HttpClient.newHttpClient();

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
    public JsonArray getJsonWebKeysFromIssuer(String issuer) throws IOException, InterruptedException {
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
            return null;
        }
        return responseBodyJson.get("keys").getAsJsonArray();
    }

    /**
     * Gets a JSON Web Key with a given key ID ('kid') from an OpenID connect issuer's /keys endpoint, returned as a JSON object
     */
    public JsonObject getJsonWebKeyFromIssuerByKeyId(String keyId, String issuer) throws IOException, InterruptedException {
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

    /**
     * Checks if a given JWT is valid or not
     */
    public boolean isJwtValid(String jwt)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        try {

            DecodedJWT decodedJwt = JWT.decode(jwt);
            RSAPublicKey publicKey = getRSAPublicKeyFromIssuer(decodedJwt.getKeyId(), issuerUrl);
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuerUrl).build();

            verifier.verify(jwt);
            return (decodedJwt != null);

        } catch (JWTVerificationException e) {
            // The JWT is not valid
            logger.info("Invalid JWT '" + jwt + "'. Reason: " + e.getMessage());
            return false;
        }
    }

    // Constructs an RSA public key from a JSON Web Key (JWK) that contains the provided key ID
    // A JWK contains the following fields:
    // {
    //   "use": "sig",
    //   "kty": "RSA",
    //   "kid": "123abc",
    //   "alg": "RS256",
    //   "n": "abcdefg",
    //   "e": "xyz"
    // }
    private RSAPublicKey getRSAPublicKeyFromIssuer(String keyId, String issuer)
            throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Get the JWK with the given key ID
        JsonObject matchingJwk = getJsonWebKeyFromIssuerByKeyId(keyId, issuer);
        if (matchingJwk == null) {
            logger.error("Error: No matching JSON Web Key was found with key ID '" + keyId + "'.");
            return null;
        }

        // A JWK contains an 'n' field to represent the key's modulus, and an 'e' field to represent the key's exponent, both are Base64URL-encoded
        Decoder decoder = Base64.getUrlDecoder();
        BigInteger modulus = new BigInteger(1, decoder.decode(matchingJwk.get("n").getAsString()));
        BigInteger exponent = new BigInteger(1, decoder.decode(matchingJwk.get("e").getAsString()));

        // Build a public key from the JWK that was matched
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey generatedPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        return generatedPublicKey;
    }

    public String getIssuer() {
        return this.issuerUrl;
    }
}
