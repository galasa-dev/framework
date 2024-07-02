/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.routes;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import dev.galasa.framework.api.authentication.internal.routes.AuthTokensDetailsRoute;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.authentication.mocks.MockDexGrpcClient;
import dev.galasa.framework.api.common.mocks.MockAuthStoreService;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.User;

public class AuthTokensDetailsRouteTest {

    @Test
    public void testAuthTokensDetailsRouteRegexMatchesExpectedPaths(){
        //Given...
        String tokensDetailsRoutePath = new AuthTokensDetailsRoute(null, null, null).getPath();

        //When...
        Pattern routePattern = Pattern.compile(tokensDetailsRoutePath);

        //Then...
        // The route should only accept /tokens/{tokenid}, where {tokenid} contains:
        // - alphanumeric characters
        // - dashes (-)
        // - underscores (_)
        assertThat(routePattern.matcher("/tokens/abc").matches()).isTrue();
        assertThat(routePattern.matcher("/tokens/123").matches()).isTrue();
        assertThat(routePattern.matcher("/tokens/1-token-2").matches()).isTrue();
        assertThat(routePattern.matcher("/tokens/token_1_2_a876").matches()).isTrue();
        assertThat(routePattern.matcher("/tokens/this-1s_4n-id").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("/tokens/hello-world!").matches()).isFalse();
        assertThat(routePattern.matcher("/token/is-missing-an-s").matches()).isFalse();
        assertThat(routePattern.matcher("/tokens/////").matches()).isFalse();
        assertThat(routePattern.matcher("/t0kens/    ").matches()).isFalse();
        assertThat(routePattern.matcher("/").matches()).isFalse();
        assertThat(routePattern.matcher("").matches()).isFalse();
    }

    @Test
    public void testDeleteAuthTokensRevokesTokenOK() throws Exception {
        // Given...
        String tokenId = "id123";
        String description = "test token";
        String clientId = "my-client";
        Instant creationTime = Instant.now();
        User owner = new User("username");

        List<IInternalAuthToken> tokens = new ArrayList<>();
        tokens.add(new MockInternalAuthToken(tokenId, description, creationTime, owner, clientId));

        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://my-issuer");
        mockDexGrpcClient.addDexClient(clientId, "my-secret", "http://a-callback-url");
        mockDexGrpcClient.addMockRefreshToken(owner.getLoginId(), clientId);

        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(null, mockDexGrpcClient, new MockFramework(authStoreService));

        Algorithm mockJwtSigningAlgorithm = Algorithm.HMAC256("dummysecret");
        String mockJwt = JWT.create()
            .withSubject(owner.getLoginId())
            .withIssuedAt(Instant.EPOCH)
            .sign(mockJwtSigningAlgorithm);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens/" + tokenId, "", "DELETE", headers);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        assertThat(authStoreService.getTokens()).hasSize(1);
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(authStoreService.getTokens()).hasSize(0);
    }

    @Test
    public void testDeleteAuthTokensWithFailingAuthStoreAccessThrowsError() throws Exception {
        // Given...
        String tokenId = "id123";
        String description = "test token";
        String clientId = "my-client";
        Instant creationTime = Instant.now();
        User owner = new User("username");

        List<IInternalAuthToken> tokens = new ArrayList<>();
        tokens.add(new MockInternalAuthToken(tokenId, description, creationTime, owner, clientId));

        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://my-issuer");
        mockDexGrpcClient.addDexClient(clientId, "my-secret", "http://a-callback-url");
        mockDexGrpcClient.addMockRefreshToken(owner.getLoginId(), clientId);

        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);

        // Throw an exception to simulate an auth store failure
        authStoreService.setThrowException(true);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(null, mockDexGrpcClient, new MockFramework(authStoreService));

        Algorithm mockJwtSigningAlgorithm = Algorithm.HMAC256("dummysecret");
        String mockJwt = JWT.create()
            .withSubject(owner.getLoginId())
            .withIssuedAt(Instant.EPOCH)
            .sign(mockJwtSigningAlgorithm);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens/" + tokenId, "", "DELETE", headers);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(outStream.toString()).contains("GAL5064E", "Failed to revoke the token with the given ID");
    }
}
