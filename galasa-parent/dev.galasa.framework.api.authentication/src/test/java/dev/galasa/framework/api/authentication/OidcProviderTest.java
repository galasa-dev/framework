/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.internal.beans.JsonWebKey;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;
import dev.galasa.framework.api.common.mocks.MockHttpSession;
import dev.galasa.framework.api.common.mocks.MockTimeService;
import dev.galasa.framework.spi.utils.GalasaGson;

public class OidcProviderTest {

    private static final GalasaGson gson = new GalasaGson();

    /**
     * Creates and returns a JSON object representing a JSON Web Key.
     * The format of the created JSON object is as follows:
     * {
     *   "keys": [
     *     { "kid": "key-id" },
     *     { "kid": "key-id" },
     *   ],
     * }
     */
    private JsonObject createMockJwkObject(String keyId) {
        JsonObject jwkJson = new JsonObject();
        jwkJson.addProperty("kid", keyId);

        return jwkJson;
    }

    private HttpResponse<Object> createMockOidcDiscoveryResponse() {
        JsonObject mockOidcConfig = new JsonObject();
        mockOidcConfig.addProperty("authorization_endpoint", "http://my-issuer/auth");
        mockOidcConfig.addProperty("token_endpoint", "http://my-issuer/token");
        mockOidcConfig.addProperty("jwks_uri", "http://my-issuer/keys");

        HttpResponse<Object> mockOidcDiscoveryResponse = new MockHttpResponse<Object>(gson.toJson(mockOidcConfig));
        return mockOidcDiscoveryResponse;
    }

    @Test
    public void testTokenPostWithRefreshTokenValidRequestReturnsValidResponse() throws Exception {
        // Given...
        JsonObject mockJwtJson = new JsonObject();
        mockJwtJson.addProperty("id_token", "this-is-a-jwt");

        HttpResponse<Object> mockTokenResponse = new MockHttpResponse<Object>(gson.toJson(mockJwtJson));

        HttpClient mockHttpClient = mock(HttpClient.class);

        String clientId = "galasa";
        String clientSecret = "abc";
        String refreshToken = "thisisarefreshtoken";

        when(mockHttpClient.send(any(), any())).thenThrow(new IOException());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockTokenResponse);

        // When...
        HttpResponse<String> response = oidcProvider.sendTokenPost(clientId, clientSecret, refreshToken);

        // Then...
        assertThat(response).isNotNull();
        assertThat(response.body()).isEqualTo(mockTokenResponse.body());
    }

    @Test
    public void testTokenPostWithAuthCodeValidRequestReturnsValidResponse() throws Exception {
        // Given...
        JsonObject mockJwtJson = new JsonObject();
        mockJwtJson.addProperty("id_token", "this-is-a-jwt");

        HttpResponse<Object> mockTokenResponse = new MockHttpResponse<Object>(gson.toJson(mockJwtJson));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockTokenResponse);

        String clientId = "galasa";
        String clientSecret = "abc";
        String authCode = "thisisacode";
        String redirectUri = "http://mock.galasa.server/auth/callback";

        when(mockHttpClient.send(any(), any())).thenThrow(new IOException());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockTokenResponse);

        // When...
        HttpResponse<String> response = oidcProvider.sendTokenPost(clientId, clientSecret, authCode, redirectUri);

        // Then...
        assertThat(response).isNotNull();
        assertThat(response.body()).isEqualTo(mockTokenResponse.body());
    }

    @Test
    public void testGetJsonWebKeyByKeyIdWithCachedKeysReturnsCachedKey() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        JsonObject mockJwks = new JsonObject();
        JsonArray keysArray = new JsonArray();

        keysArray.add(createMockJwkObject("thisisakey"));
        keysArray.add(createMockJwkObject("thisisanotherkey"));
        keysArray.add(createMockJwkObject(targetKeyId));
        mockJwks.add("keys", keysArray);

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockJwks));

        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(createMockOidcDiscoveryResponse());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient, mockTimeService);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        // When...
        oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Other calls shouldn't send a request to the issuer as long as the refresh interval hasn't elapsed
        oidcProvider.getJsonWebKeyByKeyId(targetKeyId);
        oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Then...
        // Check that the HTTP client only sent one request
        verify(mockHttpClient, times(1)).send(any(), any());
    }

    @Test
    public void testGetJsonWebKeyByKeyIdWithCachedKeysRefreshesCache() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        JsonObject mockJwks = new JsonObject();
        JsonArray keysArray = new JsonArray();

        keysArray.add(createMockJwkObject("thisisakey"));
        keysArray.add(createMockJwkObject("thisisanotherkey"));
        keysArray.add(createMockJwkObject(targetKeyId));
        mockJwks.add("keys", keysArray);

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockJwks));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(createMockOidcDiscoveryResponse());

        // First cache refresh
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient, mockTimeService);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        // When...
        oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Bump up the current time by a day to force a cache refresh
        mockTimeService.setCurrentTime(Instant.now().plus(1, ChronoUnit.DAYS));

        // Get the keys again
        oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Then...
        // Check that the HTTP client only sent one request
        verify(mockHttpClient, times(2)).send(any(), any());
    }

    @Test
    public void testGetJsonWebKeyByKeyIdReturnsOk() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        JsonObject mockJwks = new JsonObject();
        JsonArray keysArray = new JsonArray();

        keysArray.add(createMockJwkObject("thisisakey"));
        keysArray.add(createMockJwkObject("thisisanotherkey"));
        keysArray.add(createMockJwkObject(targetKeyId));
        mockJwks.add("keys", keysArray);

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockJwks));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(createMockOidcDiscoveryResponse());

        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient, mockTimeService);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        // When...
        JsonWebKey key = oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Then...
        assertThat(key).isNotNull();
        assertThat(key.getKeyId()).isEqualTo(targetKeyId);
    }

    @Test
    public void testGetJsonWebKeyByKeyIdWithNoMatchingKeyReturnsNull() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        JsonObject mockJwks = new JsonObject();
        JsonArray keysArray = new JsonArray();

        keysArray.add(createMockJwkObject("thisisakey"));
        keysArray.add(createMockJwkObject("thisisanotherkey"));
        mockJwks.add("keys", keysArray);

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockJwks));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(createMockOidcDiscoveryResponse());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        // When...
        JsonWebKey key = oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Then...
        assertThat(key).isNull();
    }

    @Test
    public void testIsJwtValidWithBadTokenFormatReturnsFalse() throws Exception {
        // Given...
        // A JWT that does not have 3 parts (should be of the form "header.payload.signature")
        String testJwt = "thisisabadjwt";

        HttpClient mockHttpClient = mock(HttpClient.class);

        when(mockHttpClient.send(any(), any())).thenThrow(new IOException());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        boolean result = oidcProvider.isJwtValid(testJwt);

        // Then...
        assertThat(result).isFalse();
    }

    @Test
    public void testGetOpenIdConfigurationReturnsValidConfig() throws Exception {
        // Given...
        JsonObject mockOpenIdConfig = new JsonObject();
        mockOpenIdConfig.addProperty("authorization_endpoint", "http://my.server/auth");
        mockOpenIdConfig.addProperty("token_endpoint", "http://my.server/token");
        mockOpenIdConfig.addProperty("jwks_uri", "http://my.server/keys");

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockOpenIdConfig), 200);

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        JsonObject openIdConfig = oidcProvider.getOpenIdConfiguration();

        // Then...
        assertThat(openIdConfig).isNotNull();
        assertThat(openIdConfig).isEqualTo(mockOpenIdConfig);
    }

    @Test
    public void testGetOpenIdConfigurationWithErrorResponseReturnsNull() throws Exception {
        // Given...
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(null, 500);

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        JsonObject openIdConfig = oidcProvider.getOpenIdConfiguration();

        // Then...
        assertThat(openIdConfig).isNull();
    }

    @Test
    public void testAuthorizationGetReturnsValidResponse() throws Exception {
        // Given...
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("", 302);

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenThrow(new IOException());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        MockHttpSession mockSession = new MockHttpSession();

        // When...
        HttpResponse<String> response = oidcProvider.sendAuthorizationGet("my-client-id", "http://my.server/callback", mockSession);

        // Then...
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(mockResponse);

        // Ensure the "state" parameter has been set as a session attribute
        String stateAttribute = (String) mockSession.getAttribute("state");
        assertThat(stateAttribute).isNotNull();
        assertThat(stateAttribute).hasSize(32);
    }

    @Test
    public void testGetConnectorRedirectUrlWithMissingLocationHeaderReturnsNull() throws Exception {
        // Given...
        Map<String, List<String>> headers = new HashMap<>();
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("", HttpHeaders.of(headers, defaultFilter));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenThrow(new IOException());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        MockHttpSession mockSession = new MockHttpSession();

        // When...
        String redirectUrl = oidcProvider.getConnectorRedirectUrl("my-client-id", "http://my.server/callback", mockSession);

        // Then...
        assertThat(redirectUrl).isNull();
    }

    @Test
    public void testGetConnectorRedirectUrlWithLocationHeaderReturnsUrl() throws Exception {
        // Given...
        String mockRedirectUrl = "http://my.connector/auth";
        Map<String, List<String>> headers = Map.of("location", List.of(mockRedirectUrl));
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("", HttpHeaders.of(headers, defaultFilter));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenThrow(new IOException());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        MockHttpSession mockSession = new MockHttpSession();

        // When...
        String redirectUrl = oidcProvider.getConnectorRedirectUrl("my-client-id", "http://my.server/callback", mockSession);

        // Then...
        assertThat(redirectUrl).isNotNull();
        assertThat(redirectUrl).isEqualTo(mockRedirectUrl);
    }

    @Test
    public void testGetConnectorRedirectUrlWithRelativeLocationHeaderReturnsAbsoluteUrl() throws Exception {
        // Given...
        String mockRedirectUrl = "http://my.issuer/auth";
        Map<String, List<String>> headers = Map.of("location", List.of("/auth"));
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(URI.create(mockRedirectUrl), HttpHeaders.of(headers, defaultFilter));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenThrow(new IOException());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        reset(mockHttpClient);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        MockHttpSession mockSession = new MockHttpSession();

        // When...
        String redirectUrl = oidcProvider.getConnectorRedirectUrl("my-client-id", "http://my.server/callback", mockSession);

        // Then...
        assertThat(redirectUrl).isNotNull();
        assertThat(redirectUrl).isEqualTo(mockRedirectUrl);
    }
}