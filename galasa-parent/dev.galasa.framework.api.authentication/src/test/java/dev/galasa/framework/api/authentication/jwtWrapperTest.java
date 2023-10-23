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

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;

public class jwtWrapperTest {

    
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
        JwtWrapper auth = new JwtWrapper(req);
        String user = auth.getUserName();
        
        // Then...
        assertThat(user).isEqualTo("validUserID");
    }

    @Test
    public void TestGetUserNoJwtReturnsError() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        //Given...
        Map<String, String> headers = Map.of("Galasa-Application", "galasactl");
        
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);
        
        // When...
        Throwable thrown = catchThrowable( () -> {new JwtWrapper(req);});
        
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
        Throwable thrown = catchThrowable( () -> {new JwtWrapper(req);});
        
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
        Throwable thrown = catchThrowable( () -> {new JwtWrapper(req);});
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("The token is null.");
    }
}