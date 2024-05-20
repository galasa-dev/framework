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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.authentication.internal.beans.JsonWebKey;
import dev.galasa.framework.api.common.ITimeService;
import dev.galasa.framework.api.common.SystemTimeService;
import dev.galasa.framework.spi.utils.GalasaGson;

/**
 * A class that handles communications with an OpenID Connect (OIDC) Provider.
 */
public class OidcProvider implements IOidcProvider {

    private final Log logger = LogFactory.getLog(getClass());

    private static final GalasaGson gson = new GalasaGson();

    private static final String BEARER_TOKEN_SCOPE = "openid offline_access profile";
    private static final int JWK_REFRESH_INTERVAL_MINUTES = 10;

    private JsonArray jsonWebKeys;
    private Instant nextJwkRefresh = Instant.EPOCH;
    private ITimeService timeService;

    private String issuerUrl;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String jwksUri;

    private HttpClient httpClient = HttpClient.newHttpClient();

    public OidcProvider(String issuerUrl, HttpClient httpClient, ITimeService timeService) {
        this.issuerUrl = issuerUrl;
        this.httpClient = httpClient;
        this.timeService = timeService;

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
        logger.info("Authorization endpoint is: " + this.authorizationEndpoint);
        logger.info("Token endpoint is: " + this.tokenEndpoint);
        logger.info("JWKs endpoint is: " + this.jwksUri);
    }

    public OidcProvider(String issuerUrl, HttpClient httpClient) {
        this(issuerUrl, httpClient, new SystemTimeService());
    }

    /**
     * Sends a GET request to an OpenID Connect provider's /.well-known/openid-configuration
     * endpoint and return the JSON response.
     */
    public JsonObject getOpenIdConfiguration() throws IOException, InterruptedException {
        String openIdConfigurationUrl = issuerUrl + "/.well-known/openid-configuration";
        JsonObject responseJson = null;

        logger.info("Sending GET request to " + openIdConfigurationUrl);
        HttpResponse<String> response = sendGetRequest(URI.create(openIdConfigurationUrl));
        if (response.statusCode() == HttpServletResponse.SC_OK) {
            logger.info("OpenID configuration received successfully");
            responseJson = gson.fromJson(response.body(), JsonObject.class);
        } else {
            logger.error("Failed to retrieve OpenID configuration from issuer");
        }
        return responseJson;
    }

    /**
     * Gets the URL of the upstream connector to authenticate with (e.g. a
     * github.com URL to authenticate with an OAuth application in GitHub).
     */
    public String getConnectorRedirectUrl(String clientId, String callbackUrl, HttpSession session) throws IOException, InterruptedException {
        logger.info("Sending GET request to " + authorizationEndpoint);

        HttpResponse<String> authResponse = sendAuthorizationGet(clientId, callbackUrl.toString(), session);
        String redirectUrl = getLocationHeaderFromResponse(authResponse);

        // In case the "Location" header contains a relative URI, get an absolute URI from the response
        if (redirectUrl != null && redirectUrl.startsWith("/")) {
            logger.info("Relative redirect URL found, converting to absolute URL");
            redirectUrl = authResponse.uri().toString();
        }
        logger.info("Retrieved redirect URL: " + redirectUrl);
        return redirectUrl;
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
        logger.info("Storing state parameter in session");
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

        logger.info("Sending POST request to '" + tokenEndpoint + "' for client with ID '" + clientId + "'");

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

        logger.info("Sending POST request to '" + tokenEndpoint + "' for client with ID '" + clientId + "'");

        // Create a POST request to the /token endpoint
        return sendPostRequest(sbRequestBody.toString(), "application/x-www-form-urlencoded", URI.create(tokenEndpoint));
    }

    /**
     * Gets a JSON array of the JSON Web Keys (JWKs) from a GET request to an issuer's /keys endpoint
     */
    private JsonArray getJsonWebKeysFromIssuer() throws IOException, InterruptedException {
        logger.info("Retrieving JSON Web Keys from issuer");
        HttpRequest getRequest = HttpRequest.newBuilder()
            .GET()
            .header("Accept", "application/json")
            .uri(URI.create(jwksUri))
            .build();

        // Send a GET request to the issuer's /keys endpoint
        HttpResponse<String> response = httpClient.send(getRequest, BodyHandlers.ofString());

        JsonObject responseBodyJson = gson.fromJson(response.body(), JsonObject.class);
        JsonArray updatedJwks = new JsonArray();
        if (responseBodyJson.has("keys")) {
            logger.info("Successfully retrieved JSON Web Keys from issuer");
            updatedJwks = responseBodyJson.get("keys").getAsJsonArray();
        } else {
            logger.error("Error: No JSON Web Keys were found at the '" + issuerUrl + "/keys' endpoint.");
        }
        return updatedJwks;
    }

    /**
     * Gets a JSON Web Key with a given key ID ('kid') from an OpenID connect issuer's /keys endpoint, returned as a JSON object
     */
    public synchronized JsonWebKey getJsonWebKeyByKeyId(String keyId) throws IOException, InterruptedException {
        // Check if it is time to refresh the cached JSON web keys
        if (jsonWebKeys == null || nextJwkRefresh.isBefore(timeService.now())) {
            logger.info("Refreshing cached JSON Web Keys");
            refreshJsonWebKeys();
        }

        // Iterate over the JSON array of JWKs, finding the one that matches the given key ID
        JsonWebKey matchingKey = null;
        for (JsonElement keyElement : jsonWebKeys) {
            JsonWebKey key = gson.fromJson(keyElement.toString(), JsonWebKey.class);
            if (key.getKeyId().equals(keyId)) {
                matchingKey = key;
                break;
            }
        }
        return matchingKey;
    }

    /**
     * Checks if a given JWT is valid or not
     */
    public boolean isJwtValid(String jwt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        boolean isValid = false;
        try {
            DecodedJWT decodedJwt = JWT.decode(jwt);

            // Try to get the public key used to sign this JWT
            RSAPublicKey publicKey = getRSAPublicKeyFromIssuer(decodedJwt.getKeyId());
            if (publicKey != null) {
                Algorithm algorithm = Algorithm.RSA256(publicKey, null);
                JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuerUrl).build();

                decodedJwt = verifier.verify(jwt);
                isValid = (decodedJwt != null);
            }

        } catch (JWTVerificationException e) {
            // The JWT is not valid
            logger.error("Invalid JWT '" + jwt + "'. Reason: " + e.getMessage(), e);
        }
        return isValid;
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
    private RSAPublicKey getRSAPublicKeyFromIssuer(String keyId)
            throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Get the JWK with the given key ID
        JsonWebKey matchingJwk = getJsonWebKeyByKeyId(keyId);
        if (matchingJwk == null) {
            // Force the cached keys to be refreshed and try again
            nextJwkRefresh = Instant.EPOCH;
            matchingJwk = getJsonWebKeyByKeyId(keyId);

            // If we still failed to get a matching key, then this must be a bad key ID
            if (matchingJwk == null) {
                logger.error("Error: No matching JSON Web Key was found with key ID '" + keyId + "'.");
                return null;
            }
        }

        // A JWK contains an 'n' field to represent the key's modulus, and an 'e' field to represent the key's exponent, both are Base64URL-encoded
        Decoder decoder = Base64.getUrlDecoder();
        BigInteger modulus = new BigInteger(1, decoder.decode(matchingJwk.getRsaModulus()));
        BigInteger exponent = new BigInteger(1, decoder.decode(matchingJwk.getRsaExponent()));

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

        String locationHeaderValue = null;
        Optional<String> locationHeader = headers.firstValue("location");
        if (locationHeader.isPresent()) {
            locationHeaderValue = locationHeader.get();
        }
        logger.info("'Location' header received from response: " + locationHeaderValue);
        return locationHeaderValue;
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


    /**
     * Refreshes the cached JSON Web Keys used to verify the signature of JWTs
     */
    private void refreshJsonWebKeys() throws IOException, InterruptedException {
        jsonWebKeys = getJsonWebKeysFromIssuer();

        // Update the next refresh time by the refresh interval
        nextJwkRefresh = Instant.now().plus(JWK_REFRESH_INTERVAL_MINUTES, ChronoUnit.MINUTES);
    }
}