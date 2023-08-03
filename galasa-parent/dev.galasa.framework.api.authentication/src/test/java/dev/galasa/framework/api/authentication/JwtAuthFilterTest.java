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

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.util.Base64.Encoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Ignore;
import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.JwtAuthFilter;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;

public class JwtAuthFilterTest extends BaseServletTest {

    class MockJwtAuthFilter extends JwtAuthFilter {

        public MockJwtAuthFilter(Environment env, HttpClient httpClient) {
            super.env = env;
            super.httpClient = httpClient;
        }
    }

    class MockFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletResponse.setStatus(200);
        }
    }

    private String createMockJwt(String issuer, int expiresAt, String keyId) {
        String header = "{ \"alg\": \"RSA256\", \"kid\": \"" + keyId + "\" }";
        String payload = "{ \"iss\": \"" + issuer + "\","+
            "\"exp\": " + expiresAt + "}";

        String signature = "dummy signature";

        Encoder encoder = Base64.getEncoder();
        String encodedHeader = encoder.encodeToString(header.getBytes());
        String encodedPayload = encoder.encodeToString(payload.getBytes());
        String encodedSignature = encoder.encodeToString(signature.getBytes());

        // To do: Base64 encode the header, payload, and signature, and then return a string of the form: "header.payload.signature"
        return encodedHeader + "." + encodedPayload + "." + encodedSignature;
    }

    @Test
    public void testRequestWithNoAuthorizationHeaderReturnsUnauthorized() throws Exception {
        // Given...
        JwtAuthFilter authFilter = new JwtAuthFilter();
        HttpServletRequest mockRequest = new MockHttpServletRequest("", "/ras/runs");
        HttpServletResponse mockResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = mockResponse.getOutputStream();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        // Then...
        assertThat(mockResponse.getStatus()).isEqualTo(401);
        checkErrorStructure(outStream.toString(), 5401, "GAL5401E", "Unauthorized");
    }

    @Test
    public void testRequestWithBadAuthorizationHeaderReturnsUnauthorized() throws Exception {
        // Given...
        JwtAuthFilter authFilter = new JwtAuthFilter();
        Map<String, String> headers = Map.of("Authorization", "badtype!");
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse mockResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = mockResponse.getOutputStream();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        // Then...
        assertThat(mockResponse.getStatus()).isEqualTo(401);
        checkErrorStructure(outStream.toString(), 5401, "GAL5401E", "Unauthorized");
    }

    @Test
    public void testRequestWithEclipseApplicationHeaderReturnsOk() throws Exception {
        // Given...
        JwtAuthFilter authFilter = new JwtAuthFilter();
        Map<String, String> headers = Map.of("Application", "eclipse");
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse mockResponse = new MockHttpServletResponse();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        // Then...
        assertThat(mockResponse.getStatus()).isEqualTo(200);
    }

    @Test
    public void testRequestWithNoAuthorizationHeaderValueReturnsUnauthorized() throws Exception {
        // Given...
        JwtAuthFilter authFilter = new JwtAuthFilter();
        Map<String, String> headers = Map.of("Authorization", "");
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse mockResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = mockResponse.getOutputStream();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        // Then...
        assertThat(mockResponse.getStatus()).isEqualTo(401);
        checkErrorStructure(outStream.toString(), 5401, "GAL5401E", "Unauthorized");
    }

    @Test
    public void testRequestToAuthPassesThroughFilterAndReturnsOk() throws Exception {
        // Given...
        JwtAuthFilter authFilter = new JwtAuthFilter();
        HttpServletRequest mockRequest = new MockHttpServletRequest("", "/auth");
        HttpServletResponse mockResponse = new MockHttpServletResponse();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        // Then...
        assertThat(mockResponse.getStatus()).isEqualTo(200);
    }

    @Ignore
    @Test
    public void testRequestWithExpiredJwtReturnsUnauthorized() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        String mockIssuerUrl = "http://dummy-issuer/dex";
        mockEnv.setenv("GALASA_DEX_ISSUER", mockIssuerUrl);

        @SuppressWarnings(value = { "unchecked" })
        HttpResponse<Object> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{ \"keys\": [{ }] }");

        HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.send(any(), any())).thenReturn(mockResponse);

        JwtAuthFilter authFilter = new MockJwtAuthFilter(mockEnv, mockClient);

        String mockJwt = createMockJwt(mockIssuerUrl, 123, "id");
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.doFilter(mockRequest, servletResponse, mockChain);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(401);
        checkErrorStructure(outStream.toString(), 5401, "GAL5401E", "Unauthorized");
    }
}
