package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;

public class OidcProviderTest {

    class MockOidcProvider extends OidcProvider {

        public MockOidcProvider(String issuerUrl, HttpClient httpClient) {
            super(issuerUrl);
            super.httpClient = httpClient;
        }
    }

    private JsonObject createMockRequestBody(String clientId, String secret, String refreshToken){
        JsonObject mockRequestBody = new JsonObject();
        mockRequestBody.addProperty("client_id", clientId);
        mockRequestBody.addProperty("secret", secret);
        mockRequestBody.addProperty("refresh_token", refreshToken);
        return mockRequestBody;
    }

    @Test
    public void testTokenPostWithValidRequestReturnsValidResponse() throws Exception {
        // Given...
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("{ \"id_token\": \"this-is-a-jwt\" }");

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        JsonObject requestBody = createMockRequestBody("galasa", "abc", "thisisarefreshtoken");
        OidcProvider oidcProvider = new MockOidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        HttpResponse<String> response = oidcProvider.sendTokenPost(requestBody);

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
}
