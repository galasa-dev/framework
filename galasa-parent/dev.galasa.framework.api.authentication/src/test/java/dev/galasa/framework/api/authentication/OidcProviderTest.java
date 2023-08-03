package dev.galasa.framework.api.authentication;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import com.google.gson.JsonObject;

import dev.galasa.Test;
import dev.galasa.framework.api.authentication.internal.OidcProvider;

public class OidcProviderTest {

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

        JsonObject requestBody = createMockRequestBody("galasa", "abc", "???");

        // When...
        mockOidcProvider.sendTokenPost(requestBody);



        // Then...
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
