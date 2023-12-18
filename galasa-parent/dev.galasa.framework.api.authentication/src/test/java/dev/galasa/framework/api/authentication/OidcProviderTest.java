/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import javax.servlet.http.Cookie;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class OidcProviderTest {

    private static final Gson gson = GalasaGsonBuilder.build();

    class MockOidcProvider extends OidcProvider {

        public MockOidcProvider(String issuerUrl, HttpClient httpClient) {
            super(issuerUrl);
            super.httpClient = httpClient;
        }
    }

    @Test
    public void testTokenPostWithValidRequestReturnsValidResponse() throws Exception {
        // Given...
        JsonObject mockJwtJson = new JsonObject();
        mockJwtJson.addProperty("id_token", "this-is-a-jwt");

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockJwtJson));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        String clientId = "galasa";
        String clientSecret = "abc";
        String refreshToken = "thisisarefreshtoken";

        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        HttpResponse<String> response = oidcProvider.sendTokenPost(clientId, clientSecret, refreshToken);

        // Then...
        assertThat(response).isNotNull();
        assertThat(response.body()).isEqualTo(mockResponse.body());
    }

    @Test
    public void testGetJsonWebKeysFromIssuerReturnsOk() throws Exception {
        // Given...
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("{" +
            "\"keys\": [" +
            "{\"kid\": \"thisisakey\"}," +
            "{\"kid\": \"thisisanotherkey\"}" +
            "]}");

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        JsonArray keys = oidcProvider.getJsonWebKeysFromIssuer(oidcProvider.getIssuer());

        // Then...
        assertThat(keys).isNotNull();
        assertThat(keys.size()).isEqualTo(2);
    }

    @Test
    public void testGetJsonWebKeysFromIssuerWithNoKeysReturnsNull() throws Exception {
        // Given...
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("{}");

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        JsonArray keys = oidcProvider.getJsonWebKeysFromIssuer(oidcProvider.getIssuer());

        // Then...
        assertThat(keys).isNull();
    }

    @Test
    public void testGetJsonWebKeysFromIssuerByKeyIdReturnsOk() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("{" +
            "\"keys\": [" +
            "{\"kid\": \"thisisakey\"}," +
            "{\"kid\": \"thisisanotherkey\"}," +
            "{\"kid\": \""+targetKeyId+"\"}" +
            "]}"
        );

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        JsonObject key = oidcProvider.getJsonWebKeyFromIssuerByKeyId(targetKeyId, oidcProvider.getIssuer());

        // Then...
        assertThat(key).isNotNull();
        assertThat(key.get("kid").getAsString()).isEqualTo(targetKeyId);
    }

    @Test
    public void testGetJsonWebKeysFromIssuerByKeyIdWithNoMatchingKeyReturnsNull() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("{" +
            "\"keys\": [" +
            "{\"kid\": \"thisisakey\"}," +
            "{\"kid\": \"thisisanotherkey\"}" +
            "]}"
        );

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        JsonObject key = oidcProvider.getJsonWebKeyFromIssuerByKeyId(targetKeyId, oidcProvider.getIssuer());

        // Then...
        assertThat(key).isNull();
    }

    @Test
    public void testIsJwtValidWithBadTokenFormatReturnsFalse() throws Exception {
        // Given...
        // A JWT that does not have 3 parts (should be of the form "header.payload.signature")
        String testJwt = "thisisabadjwt";

        HttpClient mockHttpClient = mock(HttpClient.class);

        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);

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

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockOpenIdConfig));

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        JsonObject openIdConfig = oidcProvider.getOpenIdConfiguration();

        // Then...
        assertThat(openIdConfig).isNotNull();
        assertThat(openIdConfig).isEqualTo(mockOpenIdConfig);
    }

    @Test
    public void testAuthorizationGetReturnsValidResponse() throws Exception {
        // Given...
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("", 302);

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);
        MockHttpServletResponse mockServletResponse = new MockHttpServletResponse();

        // When...
        HttpResponse<String> response = oidcProvider.sendAuthorizationGet("my-client-id", "http://my.server/callback", mockServletResponse);

        List<Cookie> responseCookies = mockServletResponse.getCookies();
        // Then...
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(mockResponse);

        // Ensure the "state" parameter has been set as a cookie
        assertThat(responseCookies).hasSize(1);
        assertThat(responseCookies.get(0).getName()).isEqualTo("state");

    }
}