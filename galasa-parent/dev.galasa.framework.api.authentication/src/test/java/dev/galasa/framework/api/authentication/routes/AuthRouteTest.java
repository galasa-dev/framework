/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.routes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class AuthRouteTest extends BaseServletTest {

    private static final Gson gson = GalasaGsonBuilder.build();

    private String buildRequestPayload(String clientId, String secret, String refreshToken, String authCode) {
        JsonObject requestPayload = new JsonObject();
        if (clientId != null) {
            requestPayload.addProperty("client_id", clientId);
        }

        if (secret != null) {
            requestPayload.addProperty("secret", secret);
        }

        if (refreshToken != null) {
            requestPayload.addProperty("refresh_token", refreshToken);
        }

        if (authCode != null) {
            requestPayload.addProperty("code", authCode);
        }

        String requestPayloadStr = gson.toJson(requestPayload);
        return requestPayloadStr;
    }

    @Test
    public void testAuthPostRequestWithEmptyRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, "", "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/auth'. Please check your request parameters or report the problem to your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E");
    }

    @Test
    public void testAuthPostRequestWithInvalidRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        // Payload with a missing "refresh_token" field
        String payload = buildRequestPayload("dummy-id", "asecret", null, null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occured when trying to execute request
        // '/auth'. Please check your request parameters or report the problem to your
        // Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E");
    }

    @Test
    public void testAuthPostRequestWithValidRefreshTokenRequestPayloadReturnsJWT() throws Exception {
        // Given...
        JsonObject responseJson = new JsonObject();
        String dummyJwt = "this-is-a-jwt";
        responseJson.addProperty("id_token", dummyJwt);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(gson.toJson(responseJson));

        String clientId = "dummy-id";
        String clientSecret = "asecret";
        String decodedSecret = new String(Base64.getDecoder().decode(clientSecret));
        String refreshToken = "here-is-a-token";

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(clientId, decodedSecret, refreshToken)).thenReturn(mockResponse);

        MockEnvironment mockEnv = new MockEnvironment();
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        String payload = buildRequestPayload(clientId, clientSecret, refreshToken, null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        mockEnv.setenv("GALASA_DEX_ISSUER", "http://dummy.host");

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "jwt" : "this-is-a-jwt",
        // }
        JsonObject expectedResponseJson = new JsonObject();
        expectedResponseJson.addProperty("jwt", dummyJwt);
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedResponseJson));
    }

    @Test
    public void testAuthPostRequestWithValidAuthCodeRequestPayloadReturnsJWT() throws Exception {
        // Given...
        JsonObject responseJson = new JsonObject();
        String dummyJwt = "this-is-a-jwt";
        responseJson.addProperty("id_token", dummyJwt);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(gson.toJson(responseJson));

        String clientId = "dummy-id";
        String clientSecret = "asecret";
        String decodedSecret = new String(Base64.getDecoder().decode(clientSecret));
        String authCode = "thisisacode";

        String redirectUri = "http://mock.galasa.server/";

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(clientId, decodedSecret, authCode, redirectUri)).thenReturn(mockResponse);

        MockEnvironment mockEnv = new MockEnvironment();
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        String payload = buildRequestPayload(clientId, clientSecret, null, authCode);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        mockEnv.setenv("GALASA_DEX_ISSUER", "http://dummy.host");

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "jwt" : "this-is-a-jwt",
        // }
        JsonObject expectedResponseJson = new JsonObject();
        expectedResponseJson.addProperty("jwt", dummyJwt);
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedResponseJson));
    }

    @Test
    public void testAuthPostRequestThrowsUnexpectedErrorReturnsServerError() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(any(), any(), any())).thenThrow(new IOException("simulating an unexpected failure!"));

        MockEnvironment mockEnv = new MockEnvironment();
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        String payload = buildRequestPayload("dummy-id", "asecret", "here-is-a-token", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        mockEnv.setenv("GALASA_DEX_ISSUER", "http://dummy.host");

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5000,
        // "error_message" : "GAL5000E: Error occured when trying to access the
        // endpoint. Report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5000, "GAL5000E", "Error occured when trying to access the endpoint");
    }

    @Test
    public void testAuthPostRequestWithTokenErrorResponseReturnsServerError() throws Exception {
        // Given...
        JsonObject dummyErrorJson = new JsonObject();
        dummyErrorJson.addProperty("error", "oh no, something went wrong!");
        HttpResponse<String> mockResponse = new MockHttpResponse<String>(gson.toJson(dummyErrorJson));

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(any(), any(), any())).thenReturn(mockResponse);

        MockEnvironment mockEnv = new MockEnvironment();
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        String payload = buildRequestPayload("dummy-id", "asecret", "here-is-a-token", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        mockEnv.setenv("GALASA_DEX_ISSUER", "http://dummy.host");

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5000,
        // "error_message" : "GAL5000E: Error occured when trying to access the
        // endpoint. Report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5000, "GAL5000E", "Error occured when trying to access the endpoint");
    }

    @Test
    public void testAuthGetRequestWithClientIdRedirectsToConnector() throws Exception {
        // Given...
        String redirectLocation = "http://my.connector/auth";
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.getConnectorRedirectUrl(any(), any(), any())).thenReturn(redirectLocation);
        when(mockOidcProvider.getIssuer()).thenReturn("http://dummy.issuer");

        MockEnvironment mockEnv = new MockEnvironment();
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        Map<String, String[]> queryParams = Map.of("clientId", new String[] { "my-client" });
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(302);
        assertThat(outStream.toString()).isEqualTo(redirectLocation);
    }

    @Test
    public void testAuthGetRequestWithEmptyReturnedLocationHeaderReturnsError() throws Exception {
        // Given...
        // No "Location" returned from the issuer, will not be able to redirect anywhere to authenticate
        Map<String, List<String>> headers = new HashMap<>();
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<String> mockResponse = new MockHttpResponse<String>("", HttpHeaders.of(headers, defaultFilter));

        String mockIssuerUrl = "http://dummy.issuer";
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendAuthorizationGet(any(), any(), any())).thenReturn(mockResponse);
        when(mockOidcProvider.getIssuer()).thenReturn(mockIssuerUrl);

        MockEnvironment mockEnv = new MockEnvironment();
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        Map<String, String[]> queryParams = Map.of("clientId", new String[] { "my-client" });
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5000,
        // "error_message" : "GAL5000E: Error occured when trying to access the
        // endpoint. Report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5000, "GAL5000E", "Error occured when trying to access the endpoint");
    }

    @Test
    public void testAuthGetRequestWithMissingClientIdReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);

        MockEnvironment mockEnv = new MockEnvironment();
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider);
        servlet.setOidcProvider(mockOidcProvider);

        Map<String, String[]> queryParams = new HashMap<>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
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
        // "/auth". Please check your request parameters or report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occured when trying to execute request");
    }
}