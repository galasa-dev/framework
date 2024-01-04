/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.routes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockHttpSession;


public class AuthCallbackRouteTest extends BaseServletTest {

    @Test
    public void testAuthCallbackGetRequestWithMissingAuthCodeAndStateReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        Map<String, String[]> queryParams = new HashMap<>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/callback");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/auth/callback'. Please check your request parameters or report the problem to your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }

    @Test
    public void testAuthCallbackGetRequestWithMissingAuthCodeReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        Map<String, String[]> queryParams = Map.of("state", new String[] { "my-state" });
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/callback");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/auth/callback'. Please check your request parameters or report the problem to your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }

    @Test
    public void testAuthCallbackGetRequestWithMissingStateReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        Map<String, String[]> queryParams = Map.of("code", new String[] { "my-auth-code" });
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/callback");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/auth/callback'. Please check your request parameters or report the problem to your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }

    @Test
    public void testAuthCallbackGetRequestWithValidStateAndCodeReturnsCode() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        String expectedCode = "my-auth-code";
        String expectedState = "my-state";
        String expectedCallbackUrl = "http://my.app";

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("code", new String[] { expectedCode });
        queryParams.put("state", new String[] { expectedState });

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("state", expectedState);
        mockSession.setAttribute("callbackUrl", expectedCallbackUrl);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/callback", mockSession);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        String expectedRedirectUrl = expectedCallbackUrl + "?code=" + expectedCode;
        assertThat(servletResponse.getStatus()).isEqualTo(302);
        assertThat(outStream.toString()).isEqualTo(expectedRedirectUrl);
    }

    @Test
    public void testAuthCallbackGetRequestWithInvalidStateReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        String expectedCode = "my-auth-code";
        String expectedState = "my-state";

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("code", new String[] { expectedCode });
        queryParams.put("state", new String[] { expectedState });

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("state", "a different state");

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/callback", mockSession);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/auth/callback'. Please check your request parameters or report the problem to your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }

    @Test
    public void testAuthCallbackGetRequestWithNoMatchingStateSessionReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        String expectedCode = "my-auth-code";
        String expectedState = "my-state";

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("code", new String[] { expectedCode });
        queryParams.put("state", new String[] { expectedState });

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("not state", "something else");

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/callback", mockSession);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/auth/callback'. Please check your request parameters or report the problem to your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }

    @Test
    public void testAuthCallbackGetRequestWithMissingStateSessionReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        String expectedCode = "my-auth-code";
        String expectedState = "my-state";

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("code", new String[] { expectedCode });
        queryParams.put("state", new String[] { expectedState });

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, "/callback");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/auth/callback'. Please check your request parameters or report the problem to your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }
}
