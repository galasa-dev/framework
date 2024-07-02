/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.routes;

import static org.assertj.core.api.Assertions.*;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.authentication.internal.routes.AuthTokensRoute;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.authentication.mocks.MockDexGrpcClient;
import dev.galasa.framework.api.authentication.mocks.MockOidcProvider;
import dev.galasa.framework.api.common.AuthToken;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockInternalAuthToken;
import dev.galasa.framework.api.common.mocks.MockTimeService;
import dev.galasa.framework.api.common.mocks.MockAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.User;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;

public class AuthTokensRouteTest extends BaseServletTest {

    private static final GalasaGson gson = new GalasaGson();

    private String buildRequestPayload(String clientId, String refreshToken, String authCode) {
        return buildRequestPayload(clientId, refreshToken, authCode, null);
    }

    private String buildRequestPayload(String clientId, String refreshToken, String authCode, String description) {
        JsonObject requestPayload = new JsonObject();
        if (clientId != null) {
            requestPayload.addProperty("client_id", clientId);
        }

        if (refreshToken != null) {
            requestPayload.addProperty("refresh_token", refreshToken);
        }

        if (authCode != null) {
            requestPayload.addProperty("code", authCode);
        }

        if (description != null) {
            requestPayload.addProperty("description", description);
        }

        String requestPayloadStr = gson.toJson(requestPayload);
        return requestPayloadStr;
    }

    private String buildTokenResponse(String jwt, String refreshToken) {
        JsonObject responseJson = new JsonObject();

        responseJson.addProperty("id_token", jwt);
        responseJson.addProperty("refresh_token", refreshToken);

        return gson.toJson(responseJson);
    }

    /**
     * Compares a list of expected authentication tokens against a JSON array
     * containing authentication tokens.
     *
     * @param expectedList    the expected ordering of tokens
     * @param actualJsonArray the actual JSON array contained in a servlet response
     */
    private void checkOrderMatches(List<AuthToken> expectedList, JsonArray actualJsonArray) {
        for (int i = 0; i < actualJsonArray.size(); i++) {
            JsonObject actualJsonObject = actualJsonArray.get(i).getAsJsonObject();

            AuthToken actualToken = gson.fromJson(actualJsonObject, AuthToken.class);
            AuthToken expectedToken = expectedList.get(i);

            // Check that all the fields of the actual token match the fields of the expected token
            assertThat(actualToken).usingRecursiveComparison().isEqualTo(expectedToken);
        }
    }

    @Test
    public void testAuthTokensRouteRegexMatchesOnlyTokens(){
        //Given...
        String tokensRoutePath = new AuthTokensRoute(null, null, null, null, null).getPath();

        //When...
        Pattern tokensRoutePattern = Pattern.compile(tokensRoutePath);

        //Then...
        // The route should only accept /tokens and /tokens/
        assertThat(tokensRoutePattern.matcher("/tokens").matches()).isTrue();
        assertThat(tokensRoutePattern.matcher("/tokens/").matches()).isTrue();

        // The route should not accept the following
        assertThat(tokensRoutePattern.matcher("/token").matches()).isFalse();
        assertThat(tokensRoutePattern.matcher("/token/").matches()).isFalse();
        assertThat(tokensRoutePattern.matcher("/tokens/////").matches()).isFalse();
        assertThat(tokensRoutePattern.matcher("/t0kens").matches()).isFalse();
        assertThat(tokensRoutePattern.matcher("/").matches()).isFalse();
        assertThat(tokensRoutePattern.matcher("").matches()).isFalse();
    }

    @Test
    public void testGetAuthTokensReturnsTokensOK() throws Exception {
        // Given...
        String tokenId = "id123";
        String description = "test token";
        String clientId = "my-client";
        Instant creationTime = Instant.now();
        User owner = new User("username");

        List<IInternalAuthToken> tokens = List.of(
            new MockInternalAuthToken(tokenId, description, creationTime, owner, clientId)
        );
        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", "", "GET");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        Map<String, String> expectedTokenFields = Map.of(
            "token_id", tokenId,
            "description", description,
            "creation_time", creationTime.toString()
        );

        checkJsonArrayStructure(getJsonArrayFromJson(output, "tokens").toString(), expectedTokenFields);
    }

    @Test
    public void testGetAuthTokensWithAuthStoreExceptionThrowsInternalServletException() throws Exception {
        // Given...
        String tokenId = "id123";
        String description = "test token";
        String clientId = "my-client";
        Instant creationTime = Instant.now();
        User owner = new User("username");

        List<IInternalAuthToken> tokens = List.of(
            new MockInternalAuthToken(tokenId, description, creationTime, owner, clientId)
        );
        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        authStoreService.setThrowException(true);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", "", "GET");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        assertThat(outStream.toString()).contains("GAL5053E", "Internal server error occurred when retrieving tokens from the auth store", "The auth store could be badly configured or could be experiencing temporary issues");
    }

    @Test
    public void testGetAuthTokensReturnsMultipleTokensOrderedByCreationTimeAscending() throws Exception {
        // Given...
        User owner = new User("username");

        Instant time1 = Instant.EPOCH;
        Instant time2 = Instant.ofEpochSecond(2000);
        Instant time3 = Instant.MAX;

        AuthToken token1 = new AuthToken("token1", "creation time after epoch", time2, owner);
        AuthToken token2 = new AuthToken("token2", "epoch creation time", time1, owner);
        AuthToken token3 = new AuthToken("token3", "future creation time", time3, owner);
        AuthToken token4 = new AuthToken("token4", "creation time after epoch, same as token1", time2, owner);

        List<IInternalAuthToken> tokens = List.of(
            new MockInternalAuthToken(token1),
            new MockInternalAuthToken(token2),
            new MockInternalAuthToken(token3),
            new MockInternalAuthToken(token4)
        );

        List<AuthToken> expectedTokenOrder = List.of(token2, token1, token4, token3);

        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", "", "GET");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkOrderMatches(expectedTokenOrder, getJsonArrayFromJson(output, "tokens"));
    }

    @Test
    public void testAuthPostRequestWithEmptyRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", "", "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5411,
        // "error_message" : "GAL5411E: ... The request body is empty."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(411);
        checkErrorStructure(outStream.toString(), 5411, "GAL5411E");
    }

    @Test
    public void testAuthPostRequestWithBadlyFormattedClientIdReturnsBadRequestError() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        // Payload with a non-alphanumeric clientID
        String payload = buildRequestPayload("&%[not a valid client ID]", "refreshtoken", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5062,
        // "error_message" : "GAL5062E: Invalid request body provided. ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5062, "GAL5062E");
    }

    @Test
    public void testAuthPostRequestWithBadlyFormattedRefreshTokenReturnsBadRequestError() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        // Payload with a non-alphanumeric refresh token
        String payload = buildRequestPayload("dummy-id", "(not a valid refresh token)", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5062,
        // "error_message" : "GAL5062E: Invalid request body provided. ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5062, "GAL5062E");
    }

    @Test
    public void testAuthPostRequestWithBadlyFormattedAuthCodeReturnsBadRequestError() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        // Payload with a non-alphanumeric auth code
        String payload = buildRequestPayload("dummy_id", "refresh-token", "$** not a valid auth code **@");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5062,
        // "error_message" : "GAL5062E: Invalid request body provided. ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5062, "GAL5062E");
    }

    @Test
    public void testAuthPostRequestWithBothRefreshTokenAndAuthCodeReturnsBadRequestError() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        // The refresh token and auth code are mutually exclusive, so a bad request should be thrown when both are
        // provided
        String payload = buildRequestPayload("dummy-id", "refresh-token", "auth-code");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5062,
        // "error_message" : "GAL5062E: Invalid request body provided. ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5062, "GAL5062E");
    }

    @Test
    public void testAuthPostRequestWithInvalidRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        // Payload with a missing "refresh_token" field
        String payload = buildRequestPayload("dummy-id", null, null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5062,
        // "error_message" : "GAL5062E: Invalid request body provided. ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5062, "GAL5062E");
    }

    @Test
    public void testAuthPostRequestWithValidRefreshTokenRequestPayloadReturnsJWT() throws Exception {
        // Given...
        String dummyJwt = "this-is-a-jwt";
        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret"; // Mock value, not a secret //pragma: allowlist secret
        String refreshToken = "here-is-a-token";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer", clientId, clientSecret, "http://callback");

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework mockFramework = new MockFramework(mockAuthStoreService);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient, mockFramework);

        String payload = buildRequestPayload(clientId, refreshToken, null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "jwt": "this-is-a-jwt",
        //   "refresh_token": "this-is-a-refresh-token",
        // }
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("jwt", dummyJwt);
        expectedJsonObject.addProperty("refresh_token", dummyRefreshToken);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJsonObject));
        assertThat(mockAuthStoreService.getTokens()).isEmpty();
    }

    @Test
    public void testAuthPostRequestWithDescriptionInRequestReturnsJWTAndStoresTokenInAuthStore() throws Exception {
        // Given...
        String tokenDescription = "my new galasactl token for my macOS laptop";
        String userName = "JohnDoe";

        Algorithm mockJwtSigningAlgorithm = Algorithm.HMAC256("dummysecret");
        String dummyJwt = JWT.create()
            .withSubject("validUserId")
            .withIssuedAt(Instant.EPOCH)
            .withClaim("name", userName)
            .sign(mockJwtSigningAlgorithm);

        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret"; // Mock value, not a secret //pragma: allowlist secret
        String authCode = "thisisacode";

        String callbackUri = "http://api.host/auth/callback";
        String issuerUrl = "http://dummy.host";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);
        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient(issuerUrl, clientId, clientSecret, callbackUri);

        Instant tokenCreationTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(tokenCreationTime);
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework mockFramework = new MockFramework(mockAuthStoreService);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient, mockFramework);

        String payload = buildRequestPayload(clientId, null, authCode, tokenDescription);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "jwt": "ey....",
        //   "refresh_token": "this-is-a-refresh-token",
        // }
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("jwt", dummyJwt);
        expectedJsonObject.addProperty("refresh_token", dummyRefreshToken);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJsonObject));

        // A record of the newly-created token should have been added to the auth store
        List<IInternalAuthToken> tokens = mockAuthStoreService.getTokens();
        assertThat(tokens).hasSize(1);

        IInternalAuthToken newToken = tokens.get(0);
        assertThat(newToken.getDescription()).isEqualTo(tokenDescription);
        assertThat(newToken.getCreationTime()).isEqualTo(tokenCreationTime);
        assertThat(newToken.getOwner().getLoginId()).isEqualTo(userName);
    }

    @Test
    public void testAuthPostRequestWithValidAuthCodeRequestPayloadReturnsJWT() throws Exception {
        // Given...
        String dummyJwt = "this-is-a-jwt";
        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret"; // Mock value, not a secret //pragma: allowlist secret
        String authCode = "thisisacode";

        String callbackUri = "http://api.host/auth/callback";
        String issuerUrl = "http://dummy.host";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient(issuerUrl, clientId, clientSecret, callbackUri);

        Instant tokenCreationTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(tokenCreationTime);
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework mockFramework = new MockFramework(mockAuthStoreService);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient, mockFramework);

        String payload = buildRequestPayload(clientId, null, authCode);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "jwt": "this-is-a-jwt",
        //   "refresh_token": "this-is-a-refresh-token",
        // }
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("jwt", dummyJwt);
        expectedJsonObject.addProperty("refresh_token", dummyRefreshToken);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJsonObject));

        // No new token records should have been added to the auth store since no token description was provided
        assertThat(mockAuthStoreService.getTokens()).isEmpty();
    }

    @Test
    public void testAuthPostRequestWithFailingAuthStoreReturnsError() throws Exception {
        // Given...
        String tokenDescription = "my new galasactl token for my macOS laptop";
        String userName = "JohnDoe";

        Algorithm mockJwtSigningAlgorithm = Algorithm.HMAC256("dummysecret");
        String dummyJwt = JWT.create()
            .withSubject("validUserId")
            .withIssuedAt(Instant.EPOCH)
            .withClaim("name", userName)
            .sign(mockJwtSigningAlgorithm);

        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret"; // Mock value, not a secret //pragma: allowlist secret
        String authCode = "thisisacode";

        String callbackUri = "http://api.host/auth/callback";
        String issuerUrl = "http://dummy.host";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient(issuerUrl, clientId, clientSecret, callbackUri);

        Instant tokenCreationTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(tokenCreationTime);
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        mockAuthStoreService.setThrowException(true);

        MockFramework mockFramework = new MockFramework(mockAuthStoreService);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient, mockFramework);

        String payload = buildRequestPayload(clientId, null, authCode, tokenDescription);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5056, "GAL5056E", "Internal server error occurred when storing the new Galasa token with description", tokenDescription);
    }

    @Test
    public void testAuthPostRequestThrowsUnexpectedErrorReturnsServerError() throws Exception {
        // Given...
        MockOidcProvider mockOidcProvider = new MockOidcProvider();
        mockOidcProvider.setThrowException(true);

        String clientId = "myclient";
        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer", clientId, "secret", "http://callback");

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient);

        String payload = buildRequestPayload("dummy-id", "here-is-a-token", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5000,
        // "error_message" : "GAL5000E: Error occured when trying to access the
        // endpoint. Report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5000, "GAL5000E", "Error occured when trying to access the endpoint");
    }

    @Test
    public void testAuthPostRequestWithTokenErrorResponseReturnsServerError() throws Exception {
        // Given...
        JsonObject dummyErrorJson = new JsonObject();
        dummyErrorJson.addProperty("error", "oh no, something went wrong!");
        HttpResponse<String> mockResponse = new MockHttpResponse<String>(gson.toJson(dummyErrorJson));

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        String clientId = "myclient";
        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer", clientId, "secret", "http://callback");

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient);

        String payload = buildRequestPayload("dummy-id", "here-is-a-token", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "error_code": 5055,
        //   "error_message": "GAL5055E: ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5055, "GAL5055E", "Failed to get a JWT and a refresh token from the Galasa Dex server", "The Dex server did not respond with a JWT and refresh token");
    }

}
