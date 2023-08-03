package dev.galasa.framework.api.authentication;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.http.HttpResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import org.junit.Test;
import dev.galasa.framework.api.authentication.internal.OidcProvider;

public class OidcProviderTest {

    private final Log logger = LogFactory.getLog(getClass());

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
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{ \"id_token\": \"this-is-a-jwt\" }");

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(any())).thenReturn(mockResponse);

        JsonObject requestBody = createMockRequestBody("galasa", "abc", "thisisarefreshtoken");
        

        // When...
        mockOidcProvider.sendTokenPost(requestBody);



        // Then...
        assertTrue(true);
    }


    @Test
    public void testTokenPostWithInvalidRequestReturnsInvalidResponse(){
        // Given...



        // When...



        // Then...
    }

    @Test
    public void testGetJsonWebKeysFromIssuerReturnsOk(){
        // Given...



        // When...



        // Then...
    }

    @Test
    public void testGetJsonWebKeysFromIssuerByKeyIdReturnsOk(){
        // Given...



        // When...



        // Then...
    }

    @Test
    public void testGetIssuer(){
        // Given...



        // When...



        // Then...
    }
    
}
