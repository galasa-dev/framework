/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;

public class JwtWrapperTest {

    // This mock JWT decodes to the following:
    // Header:
    // {
    // "alg": "HS256",
    // "typ": "JWT"
    // }
    // Payload:
    // {
    // "sub": "validUserID",
    // "name": "User Name",
    // "iat": 1516239022
    // }
    private String mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ2YWxpZFVzZXJJRCIsIm5hbWUiOiJVc2VyIE5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.UvI3VPNyTJuql6vU3ES0zsvlXdiJYzkjIRhNahD3yd8";// Mock JWT containing no data, not a secret //pragma: allowlist secret

    @Test
    public void testGetUsernameValidJwtReturnsOk() throws Exception {
        // Given...
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "name";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        String user = auth.getUsername();

        // Then...
        assertThat(user).isEqualTo("User Name");
    }

    @Test
    public void testGetUsernameValidJwtWithOnlySubClaimReturnsSubClaim() throws Exception {
        // Given...
        // This mock JWT only contains the following claims in its payload:
        // {
        // "sub": "validUserID",
        // "iat": 1516239022
        // }
        String mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ2YWxpZFVzZXJJRCIsImlhdCI6MTUxNjIzOTAyMn0.wKWJlzB3RVJJfrP8JQdL2FSIcaXOLtBJOi3rraHr0CM"; // Mock JWT, not a secret //pragma: allowlist secret
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "name,sub";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        String user = auth.getUsername();

        // Then...
        assertThat(user).isEqualTo("validUserID");
    }

    @Test
    public void testGetUsernameValidJwtWithNoMatchingClaimThrowsError() throws Exception {
        // Given...
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "non-existant-claim";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        InternalServletException thrown = catchThrowableOfType(() -> auth.getUsername(), InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5057E", "No JWT claim exists in the given JWT that matches the supplied claims:", userNameClaimOverrides);
    }

    @Test
    public void testGetUsernameValidJwtNoUserClaimsThrowsError() throws Exception {
        // Given...
        // This mock JWT contains the following claims in its payload:
        // {
        // "iat": 1516239022
        // }
        String mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE1MTYyMzkwMjJ9.tbDepxpstvGdW8TC3G8zg4B6rUYAOvfzdceoH48wgRQ"; // Mock JWT, not a secret //pragma: allowlist secret
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        // When...
        MockEnvironment mockEnv = new MockEnvironment();
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        InternalServletException thrown = catchThrowableOfType(() -> auth.getUsername(), InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5058E", "No JWT claims to retrieve a username from were provided");
    }

    @Test
    public void testGetUsernameValidJwtWithCustomClaimsReturnsUsername() throws Exception {
        // Given...
        // This mock JWT contains the following claims in its payload:
        // {
        // "sub": "subject-bob",
        // "custom_user_id": "UserId",
        // "iat": 1516239022,
        // "name": "Bob"
        // }
        String mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdWJqZWN0LWJvYiIsImN1c3RvbV91c2VyX2lkIjoiVXNlcklkIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiQm9iIn0.mC7UNlGhDW00ZkrSnDT-lQE8JMFnPuIO1MFaN_9a43E";// Mock JWT, not a secret //pragma: allowlist secret

        // Provide a custom claim to override the default JWT claims used by the API
        // server
        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "non_existant_claim,another_claim,custom_user_id,name";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        String user = auth.getUsername();

        // Then...
        assertThat(user).isEqualTo("UserId");
    }

    @Test
    public void testGetUsernameValidJwtWithSpacedCustomClaimsReturnsUsername() throws Exception {
        // Given...
        // This mock JWT contains the following claims in its payload:
        // {
        // "sub": "subject-bob",
        // "custom_user_id": "UserId",
        // "iat": 1516239022,
        // "name": "Bob"
        // }
        String mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdWJqZWN0LWJvYiIsImN1c3RvbV91c2VyX2lkIjoiVXNlcklkIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiQm9iIn0.mC7UNlGhDW00ZkrSnDT-lQE8JMFnPuIO1MFaN_9a43E"; // Mock JWT, not a secret //pragma: allowlist secret

        // Provide a custom claim to override the default JWT claims used by the API
        // server
        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "a_claim   ,    custom_user_id   ,   name";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        String user = auth.getUsername();

        // Then...
        assertThat(user).isEqualTo("UserId");
    }

    @Test
    public void testGetUsernameNoJwtReturnsError() throws Exception {
        // Given...
        MockHttpServletRequest req = new MockHttpServletRequest("", new HashMap<>());

        // When...
        Throwable thrown = catchThrowable( () -> {new JwtWrapper(req);});

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("The token is null.");
    }

    @Test
    public void testGetUsernameInvalidJwtReturnsError() throws Exception {
        // Given...
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
    public void testGetUsernameInvalidAuthorizationReturnsError() throws Exception {
        // Given...
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