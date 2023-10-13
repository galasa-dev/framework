/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;

public class AuthorizationTest {

    /*
     * Create a mock OIDC to overwrite in the MockAuthorization
     */
    class MockOidcProvider extends OidcProvider {

        public MockOidcProvider(String issuerUrl) throws IOException, InterruptedException {
            super(issuerUrl);
        }

        @Override
        public DecodedJWT decodeJwt(String jwt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException{
            DecodedJWT decodedJwt = JWT.decode(jwt);
        return decodedJwt;
        }
    }


    class MockAuthorization extends Authorization{
        
        public MockAuthorization (HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException{
            super(req);
            super.decodeJwt();
        }
    }
    
    String mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ2YWxpZFVzZXJJRCIsIm5hbWUiOiJVc2VyIE5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.UvI3VPNyTJuql6vU3ES0zsvlXdiJYzkjIRhNahD3yd8";
    /* mockJwt Contents
     * Header
     * {
     * "alg": "HS256",
     * "typ": "JWT"
     * }
     * Payload
     * {
     * "sub": "validUserID",
     * "name": "User Name",
     * "iat": 1516239022
     * }
     */

    @Test
    public void TestGetUserValidJwtReturnsOk() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        //Given...
        Map<String, String> headers = Map.of("Authorization", "Bearer "+mockJwt, 
                                            "Galasa-Application", "galasactl");
        
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);
        
        // When...
        Authorization auth = new MockAuthorization(req);
        String user = auth.getUser();
        
        // Then...
        assertThat(user).isEqualTo("validUserID");
    }

    @Test
    public void TestGetUserNoJwtReturnsError() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        //Given...
        Map<String, String> headers = Map.of("Galasa-Application", "galasactl");
        
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);
        
        // When...
        Throwable thrown = catchThrowable( () -> {new MockAuthorization(req);});
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("The token is null.");
    }

    @Test
    public void TestGetUserInvalidJwtReturnsError() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        //Given...
        Map<String, String> headers = Map.of("Authorization", "Bearer JzdWIiOiJ2YWxpZFVzZXJJRCIsIm5hbWUiOiJVc", 
                                            "Galasa-Application", "galasactl");
        
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);
        
        // When...
        Throwable thrown = catchThrowable( () -> {new MockAuthorization(req);});
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("The token was expected to have");
    }

    @Test
    public void TestGetUserInvalidAuthorizationReturnsError() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        //Given...
        Map<String, String> headers = Map.of("Authorization", "Basic JzdWIiOiJ2YWxpZFVzZXJJRCIsIm5hbWUiOiJVc", 
                                            "Galasa-Application", "galasactl");
        
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);
        
        // When...
        Throwable thrown = catchThrowable( () -> {new MockAuthorization(req);});
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("The token is null.");
    }
}