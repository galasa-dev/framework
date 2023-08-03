/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpResponse;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.AuthenticationServlet;
import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;

public class AuthenticationServletTest extends BaseServletTest {

    class MockAuthenticationServlet extends AuthenticationServlet {

        public MockAuthenticationServlet(Environment env, OidcProvider oidcProvider) {
            super.env = env;
            super.oidcProvider = oidcProvider;
        }
    }

    private String buildRequestPayload(String clientId, String secret, String refreshToken) {
        String requestPayload = "{ "+
            "\"client_id\": \""+clientId+"\", "+
            "\"secret\": \"" +secret+"\", "+
            "\"refresh_token\": \""+refreshToken+"\""+
        " }";

        return requestPayload;
    }

    @Test
    public void testAuthPostRequestWithEmptyRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        AuthenticationServlet servlet = new AuthenticationServlet();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("", "/auth");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "error_code" : 5400,
        //   "error_message" : "GAL5400E: Error occured when trying to execute request '/auth'. Please check your request parameters or report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "/auth");
    }

    @Test
    public void testAuthPostRequestWithInvalidRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        AuthenticationServlet servlet = new AuthenticationServlet();

        // Payload with a missing "refresh_token" field
        String payload = "{ \"client_id\": \"dummy-id\", \"secret\": \"asecret\" }";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(payload, "/auth");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "error_code" : 5400,
        //   "error_message" : "GAL5400E: Error occured when trying to execute request '/auth'. Please check your request parameters or report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "/auth");
    }

    @Test
    public void testAuthPostRequestWithValidRequestPayloadReturnsJWT() throws Exception {
        // Given...
        // Mock out Http requests and responses
        @SuppressWarnings(value = { "unchecked" })
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{ \"id_token\": \"this-is-a-jwt\" }");

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(any())).thenReturn(mockResponse);

        MockEnvironment mockEnv = new MockEnvironment();
        AuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);

        String payload = buildRequestPayload("dummy-id", "asecret", "here-is-a-token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(payload, "/auth");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        mockEnv.setenv("GALASA_DEX_ISSUER", "http://dummy.host");

        // When...
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "jwt" : "this-is-a-jwt",
        // }
        String expectedResponseBody = "{\"jwt\": \"this-is-a-jwt\"}";
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedResponseBody);
    }

    @Test
    public void testAuthPostRequestWithTokenErrorResponseReturnsServerError() throws Exception {
        // Given...
        // Mock out Http requests and responses
        @SuppressWarnings(value = { "unchecked" })
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{ \"error\": \"oh no, something went wrong!\" }");

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(any())).thenReturn(mockResponse);

        MockEnvironment mockEnv = new MockEnvironment();
        AuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);

        String payload = buildRequestPayload("dummy-id", "asecret", "here-is-a-token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(payload, "/auth");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        mockEnv.setenv("GALASA_DEX_ISSUER", "http://dummy.host");

        // When...
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "error_code" : 5000,
        //   "error_message" : "GAL5000E: Error occured when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E",
			"Error occured when trying to access the endpoint"
		);
    }
}
