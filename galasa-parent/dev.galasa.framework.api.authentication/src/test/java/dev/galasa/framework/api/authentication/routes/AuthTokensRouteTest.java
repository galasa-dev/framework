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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.internal.routes.AuthTokensRoute;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.common.AuthToken;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockAuthStoreService;
import dev.galasa.framework.spi.auth.IAuthToken;
import dev.galasa.framework.spi.auth.User;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.api.common.mocks.MockFramework;

public class AuthTokensRouteTest extends BaseServletTest {

    private static final GalasaGson gson = new GalasaGson();

    /**
     * Compares a list of expected authentication tokens against a JSON array
     * containing authentication tokens.
     *
     * @param expectedList    the expected ordering of tokens
     * @param actualJsonArray the actual JSON array contained in a servlet response
     */
    private void checkOrderMatches(List<AuthToken> expectedList, JsonArray actualJsonArray) {
        for (int i = 0; i < actualJsonArray.size(); i++) {
            JsonObject actualJsonObject = actualJsonArray.get(i).getAsJsonObject();

            AuthToken actualToken = gson.fromJson(actualJsonObject, AuthToken.class);
            AuthToken expectedToken = expectedList.get(i);

            // Check that all the fields of the actual token match the fields of the expected token
            assertThat(actualToken).usingRecursiveComparison().isEqualTo(expectedToken);
        }
    }

    @Test
    public void testAuthTokensRouteRegexMatchesOnlyTokens(){
        //Given...
        MockAuthStoreService authStoreService = new MockAuthStoreService(new ArrayList<>());
        String tokensRoutePath = new AuthTokensRoute(null, authStoreService).getPath();

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

        List<IAuthToken> tokens = List.of(
            new AuthToken(tokenId, description, creationTime, owner)
        );
        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

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

        checkJsonArrayStructure(getJsonArrayFromJson(output, "tokens").toString(), expectedTokenFields);
    }

    @Test
    public void testGetAuthTokensWithAuthStoreExceptionThrowsInternalServletException() throws Exception {
        // Given...
        String tokenId = "id123";
        String description = "test token";
        Instant creationTime = Instant.now();
        User owner = new User("username");

        List<IAuthToken> tokens = List.of(
            new AuthToken(tokenId, description, creationTime, owner)
        );
        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        authStoreService.setThrowException(true);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/tokens", "", "GET");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        assertThat(servletResponse.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(outStream.toString()).contains("GAL5053E", "Error retrieving tokens from the auth store");
    }

    @Test
    public void testGetAuthTokensReturnsMultipleTokensOrderedByCreationTimeAscending() throws Exception {
        // Given...
        User owner = new User("username");

        Instant time1 = Instant.EPOCH;
        Instant time2 = Instant.ofEpochSecond(2000);
        Instant time3 = Instant.MAX;

        AuthToken token1 = new AuthToken("token1", "creation time after epoch", time2, owner);
        AuthToken token2 = new AuthToken("token2", "epoch creation time", time1, owner);
        AuthToken token3 = new AuthToken("token3", "future creation time", time3, owner);
        AuthToken token4 = new AuthToken("token4", "creation time after epoch, same as token1", time2, owner);

        List<IAuthToken> tokens = List.of(token1, token2, token3, token4);
        List<AuthToken> expectedTokenOrder = List.of(token2, token1, token4, token3);

        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

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
        checkOrderMatches(expectedTokenOrder, getJsonArrayFromJson(output, "tokens"));
    }
}
