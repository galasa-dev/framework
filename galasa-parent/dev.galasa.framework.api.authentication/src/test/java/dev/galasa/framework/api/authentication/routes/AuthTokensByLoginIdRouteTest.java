/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.routes;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.beans.AuthToken;
import dev.galasa.framework.api.beans.User;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.InternalUser;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockInternalAuthToken;
import dev.galasa.framework.api.common.mocks.MockAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;
import dev.galasa.framework.api.common.mocks.MockFramework;

public class AuthTokensByLoginIdRouteTest extends BaseServletTest {

    Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

    @Test
    public void testTokenByLoginIdGetRequestWithNullQueryParamReturnsBadRequest() throws Exception {

        // Given...
        String requestorLoginId = null;
        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { requestorLoginId });

        List<IInternalAuthToken> tokens = Collections.emptyList();

        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/getTokensByLoginId");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E",
                "Error occured when trying to execute request '/getTokensByLoginId'. Please check your request parameters or report the problem to your Galasa Ecosystem owner.");
    }

    @Test
    public void testTokenByLoginIdGetRequestWithValidQueryParamReturnsOK() throws Exception {

        // Given...
        String requestorLoginId = "admin";
        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { requestorLoginId });

        User owner = new User(requestorLoginId);

        Instant time1 = Instant.EPOCH;
        Instant time2 = Instant.ofEpochSecond(2000);
        Instant time3 = Instant.MAX;

        AuthToken token1 = new AuthToken("token1", "creation time after epoch", time2, owner);
        AuthToken token2 = new AuthToken("token2", "epoch creation time", time1, owner);
        AuthToken token3 = new AuthToken("token3", "future creation time", time3, owner);
        AuthToken token4 = new AuthToken("token4", "creation time after epoch, same as token1", time2, owner);

        List<IInternalAuthToken> tokens = List.of(
                new MockInternalAuthToken(token1),
                new MockInternalAuthToken(token2),
                new MockInternalAuthToken(token3),
                new MockInternalAuthToken(token4));

        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/getTokensByLoginId");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        assertThat(getJsonArrayFromJson(output, "tokens")).hasSize(4);
    }

    @Test
    public void testTokenByLoginIdGetRequestWithValidQueryParamButDifferentOwnersReturnsOK() throws Exception {

        // Given...
        String requestorLoginId = "admin";
        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { requestorLoginId });

        User actualOwner = new User(requestorLoginId);
        User someOtherUser = new User("someOtherUser");

        Instant time1 = Instant.EPOCH;
        Instant time2 = Instant.ofEpochSecond(2000);
        Instant time3 = Instant.MAX;

        AuthToken token1 = new AuthToken("token1", "creation time after epoch", time2, actualOwner);
        AuthToken token2 = new AuthToken("token2", "epoch creation time", time1, someOtherUser);
        AuthToken token3 = new AuthToken("token3", "future creation time", time3, actualOwner);
        AuthToken token4 = new AuthToken("token4", "creation time after epoch, same as token1", time2, someOtherUser);

        List<IInternalAuthToken> tokens = List.of(
                new MockInternalAuthToken(token1),
                new MockInternalAuthToken(token2),
                new MockInternalAuthToken(token3),
                new MockInternalAuthToken(token4));

        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/getTokensByLoginId");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        assertThat(getJsonArrayFromJson(output, "tokens")).isNotEmpty();
        assertThat(getJsonArrayFromJson(output, "tokens")).hasSize(2);
    }

    @Test
    public void testGetAuthTokensByLoginIdWithAuthStoreExceptionThrowsInternalServletException() throws Exception {
        // Given...
        String tokenId = "id123";
        String description = "test token";
        String clientId = "my-client";
        Instant creationTime = Instant.now();
        IInternalUser owner = new InternalUser("username", "dexId");

        Map<String, String[]> queryParams = new HashMap<>();

        queryParams.put("loginId", new String[] { "username" });

        List<IInternalAuthToken> tokens = List.of(
            new MockInternalAuthToken(tokenId, description, creationTime, owner, clientId)
        );
        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);
        authStoreService.setThrowException(true);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(new MockFramework(authStoreService));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams,"/getTokensByLoginId");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        assertThat(outStream.toString()).contains("GAL5053E", "Internal server error occurred when retrieving tokens from the auth store", "The auth store could be badly configured or could be experiencing temporary issues");
    }

}
