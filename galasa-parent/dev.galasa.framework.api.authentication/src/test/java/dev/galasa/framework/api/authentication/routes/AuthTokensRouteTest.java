/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.routes;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.routes.AuthTokensRoute;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockUserStoreService;
import dev.galasa.framework.spi.auth.AuthToken;
import dev.galasa.framework.spi.auth.User;
import dev.galasa.framework.api.common.mocks.MockFramework;

public class AuthTokensRouteTest extends BaseServletTest {

    private MockEnvironment mockEnv;

    @Before
    public void setUp() {
        mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);
    }

	@Test
	public void testAuthTokensRouteRegexMatchesOnlyTokens(){
		//Given...
        MockUserStoreService userStoreService = new MockUserStoreService(null);
        String tokensRoutePath = new AuthTokensRoute(null, new MockFramework(userStoreService)).getPath();

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
        Instant creationTime = Instant.now();
        User owner = new User("username");

        List<AuthToken> tokens = List.of(
            new AuthToken(tokenId, description, creationTime, owner)
        );
        MockUserStoreService userStoreService = new MockUserStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, new MockFramework(userStoreService));

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
        assertThat(servletResponse.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

        Map<String, String> expectedTokenFields = Map.of(
            "token_id", tokenId,
            "description", description,
            "creation_time", creationTime.toString()
        );

        checkJsonArrayStructure(getJsonArrayAsStringFromJson(output, "tokens"), expectedTokenFields);
    }
}
