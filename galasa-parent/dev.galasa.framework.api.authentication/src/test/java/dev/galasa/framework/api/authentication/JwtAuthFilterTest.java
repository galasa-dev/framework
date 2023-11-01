/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;

public class JwtAuthFilterTest extends BaseServletTest {

    class MockJwtAuthFilter extends JwtAuthFilter {

        public MockJwtAuthFilter(Environment env, OidcProvider oidcProvider) {
            super.env = env;
            super.oidcProvider = oidcProvider;
        }
    }

    class MockFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletResponse.setStatus(200);
        }
    }

    @Test
    public void testRequestWithNoAuthorizationHeaderReturnsUnauthorized() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        String mockIssuerUrl = "http://dummy-issuer/dex";
        mockEnv.setenv("GALASA_DEX_ISSUER", mockIssuerUrl);

        JwtAuthFilter authFilter = new JwtAuthFilter();
        Map<String, String> headers = Map.of("Galasa-Application", "galasactl");
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse mockResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = mockResponse.getOutputStream();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.init(null);
        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        // Then...
        assertThat(mockResponse.getStatus()).isEqualTo(401);
        checkErrorStructure(outStream.toString(), 5401, "GAL5401E", "Unauthorized");
    }

    @Test
    public void testRequestWithBadAuthorizationHeaderReturnsUnauthorized() throws Exception {
        // Given...
        JwtAuthFilter authFilter = new JwtAuthFilter();
        Map<String, String> headers = Map.of("Authorization", "badtype!",
                                             "Galasa-Application", "galasactl");
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse mockResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = mockResponse.getOutputStream();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.init(null);
        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        // Then...
        assertThat(mockResponse.getStatus()).isEqualTo(401);
        checkErrorStructure(outStream.toString(), 5401, "GAL5401E", "Unauthorized");
    }

    @Test
    public void testRequestWithNoAuthorizationHeaderValueReturnsUnauthorized() throws Exception {
        // Given...
        JwtAuthFilter authFilter = new JwtAuthFilter();
        Map<String, String> headers = Map.of("Authorization", "",
                                             "Galasa-Application", "galasactl");
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse mockResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = mockResponse.getOutputStream();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.init(null);
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
        authFilter.init(null);
        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        // Then...
        assertThat(mockResponse.getStatus()).isEqualTo(200);
    }

    @Test
    public void testRequestWithInvalidJwtReturnsUnauthorized() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        String mockIssuerUrl = "http://dummy-issuer/dex";
        mockEnv.setenv("GALASA_DEX_ISSUER", mockIssuerUrl);

        String mockJwt = "dummy.jwt.here";
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.isJwtValid(mockJwt)).thenReturn(false);

        JwtAuthFilter authFilter = new MockJwtAuthFilter(mockEnv, mockOidcProvider);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt,
                                             "Galasa-Application", "galasactl");
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

    @Test
    public void testRequestWithExceptionGetsCaught() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        String mockIssuerUrl = "http://dummy-issuer/dex";
        mockEnv.setenv("GALASA_DEX_ISSUER", mockIssuerUrl);

        String mockJwt = "dummy.jwt.here";
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.isJwtValid(mockJwt)).thenThrow(new IOException("simulating a failure!"));

        JwtAuthFilter authFilter = new MockJwtAuthFilter(mockEnv, mockOidcProvider);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt,
                                             "Galasa-Application", "galasactl");
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.doFilter(mockRequest, servletResponse, mockChain);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5000, "GAL5000E", "Error occured when trying to access the endpoint");
    }

    @Test
    public void testRequestWithValidJwtReturnsOk() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        String mockIssuerUrl = "http://dummy-issuer/dex";
        mockEnv.setenv("GALASA_DEX_ISSUER", mockIssuerUrl);

        String mockJwt = "dummy.jwt.here";

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.isJwtValid(mockJwt)).thenReturn(true);

        JwtAuthFilter authFilter = new MockJwtAuthFilter(mockEnv, mockOidcProvider);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt,
                                             "Galasa-Application", "galasactl");
        HttpServletRequest mockRequest = new MockHttpServletRequest("/ras/runs", headers);
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        FilterChain mockChain = new MockFilterChain();

        // When...
        authFilter.doFilter(mockRequest, servletResponse, mockChain);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(200);
    }
}