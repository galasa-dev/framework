/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
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
import com.google.gson.JsonSyntaxException;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/**
 * A class that handles communications with an OpenID Connect (OIDC) Provider.
 */
public class OidcProvider {

    private final Log logger = LogFactory.getLog(getClass());

    private static final Gson gson = GalasaGsonBuilder.build();

    private String issuerUrl;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String jwksUri;

    private HttpClient httpClient = HttpClient.newHttpClient();

    private static final String BEARER_TOKEN_SCOPE = "openid offline_access profile";

    public OidcProvider(String issuerUrl, HttpClient httpClient) {
        this.issuerUrl = issuerUrl;
        this.httpClient = httpClient;

        this.authorizationEndpoint = issuerUrl + "/auth";
        this.tokenEndpoint = issuerUrl + "/token";
        this.jwksUri = issuerUrl + "/keys";

        try {
            JsonObject openIdConfiguration = getOpenIdConfiguration();
            if (openIdConfiguration != null) {
                // The following endpoints are mandatory in OpenID configurations
                this.authorizationEndpoint = openIdConfiguration.get("authorization_endpoint").getAsString();
                this.tokenEndpoint = openIdConfiguration.get("token_endpoint").getAsString();
                this.jwksUri = openIdConfiguration.get("jwks_uri").getAsString();
            }
        } catch (IOException | InterruptedException | JsonSyntaxException e) {
            logger.error("Unable to obtain issuer's OpenID configuration, using defaults");
        }
    }

    public String getIssuer() {
        return this.issuerUrl;
    }

    /**
     * Sends a GET request to an OpenID Connect provider's
     * /.well-known/openid-configuration endpoint
     */
    public JsonObject getOpenIdConfiguration() throws IOException, InterruptedException {
        String openIdConfigurationUrl = issuerUrl + "/.well-known/openid-configuration";
        HttpResponse<String> response = sendGetRequest(URI.create(openIdConfigurationUrl));
        if (response.statusCode() == HttpServletResponse.SC_OK) {
            return gson.fromJson(response.body(), JsonObject.class);
        }
        return null;
    }

    /**
     * Gets the URL of the upstream connector to authenticate with (e.g. a
     * github.com URL to authenticate with an OAuth application in GitHub).
     */
    public String getConnectorRedirectUrl(String clientId, String callbackUrl, HttpSession session) throws IOException, InterruptedException {
        HttpResponse<String> authResponse = sendAuthorizationGet(clientId, callbackUrl.toString(), session);

        // Construct the auth URL from the "Location" header in the response from the
        // OpenID Connect provider
        URI authUrl = URI.create(issuerUrl);
        String location = getLocationHeaderFromResponse(authResponse);

        if (location != null) {
            authUrl = authUrl.resolve(location);
            authResponse = sendGetRequest(authUrl);
            return getLocationHeaderFromResponse(authResponse);
        }
        return null;
    }

    /**
     * Sends a GET request to an OpenID Connect authorization endpoint, returning
     * the received response.
     */
    public HttpResponse<String> sendAuthorizationGet(String clientId, String callbackUrl, HttpSession session) throws IOException, InterruptedException {
        String state = RandomStringUtils.randomAlphanumeric(32);
        String queryParams = "?response_type=code"
            + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
            + "&redirect_uri=" + callbackUrl
            + "&scope=" + URLEncoder.encode(BEARER_TOKEN_SCOPE, StandardCharsets.UTF_8)
            + "&state=" + state;

        // Save the state in a session for validation later
        session.setAttribute("state", state);

        String authUrl = authorizationEndpoint + queryParams;
        return sendGetRequest(URI.create(authUrl));
    }

    /**
     * Sends a POST request with a given request body to an OpenID Connect /token endpoint, returning the received response.
     */
    public HttpResponse<String> sendTokenPost(String clientId, String clientSecret, String refreshToken) throws IOException, InterruptedException {

        StringBuilder sbRequestBody = new StringBuilder();

        // Convert the request's JSON object to the application/x-www-form-urlencoded content type
        // as required by the /token endpoint.
        sbRequestBody.append("grant_type=refresh_token");
        sbRequestBody.append("&client_id=" + clientId);
        sbRequestBody.append("&client_secret=" + clientSecret);
        sbRequestBody.append("&refresh_token=" + refreshToken);

        // Create a POST request to the /token endpoint
        return sendPostRequest(sbRequestBody.toString(), "application/x-www-form-urlencoded", URI.create(tokenEndpoint));
    }

    /**
     * Sends a POST request with a given request body to an OpenID Connect /token endpoint, returning the received response.
     */
    public HttpResponse<String> sendTokenPost(String clientId, String clientSecret, String authCode, String redirectUri) throws IOException, InterruptedException {

        StringBuilder sbRequestBody = new StringBuilder();

        // Convert the request's JSON object to the application/x-www-form-urlencoded content type
        // as required by the /token endpoint.
        sbRequestBody.append("grant_type=authorization_code");
        sbRequestBody.append("&code=" + authCode);
        sbRequestBody.append("&client_id=" + clientId);
        sbRequestBody.append("&client_secret=" + clientSecret);
        sbRequestBody.append("&redirect_uri=" + redirectUri);

        // Create a POST request to the /token endpoint
        return sendPostRequest(sbRequestBody.toString(), "application/x-www-form-urlencoded", URI.create(tokenEndpoint));
    }

    /**
     * Gets a JSON array of the JSON Web Keys (JWKs) from a GET request to an issuer's /keys endpoint
     */
    public JsonArray getJsonWebKeysFromIssuer(String issuer) throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
            .GET()
            .header("Accept", "application/json")
                .uri(URI.create(jwksUri))
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
    public boolean isJwtValid(String jwt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
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

    /**
     * Returns the value of the "Location" HTTP header in a given HTTP response if
     * one exists, returns null otherwise.
     */
    private String getLocationHeaderFromResponse(HttpResponse<String> response) {
        HttpHeaders headers = response.headers();

        Optional<String> locationHeader = headers.firstValue("location");
        if (locationHeader.isPresent()) {
            return locationHeader.get();
        }
        return null;
    }

    /**
     * Sends a POST request to a given URI and returns the response received
     */
    private HttpResponse<String> sendPostRequest(String requestBody, String contentType, URI uri) throws IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
            .POST(BodyPublishers.ofString(requestBody))
            .header("Content-Type", contentType)
            .uri(uri)
            .build();

        return httpClient.send(postRequest, BodyHandlers.ofString());
    }

    /**
     * Sends a GET request to a given URI and returns the response received
     */
    private HttpResponse<String> sendGetRequest(URI uri) throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder().GET().uri(uri).build();
        return httpClient.send(getRequest, BodyHandlers.ofString());
    }
}