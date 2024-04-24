/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.routes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.authentication.mocks.MockDexGrpcClient;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockHttpSession;
import dev.galasa.framework.spi.utils.GalasaGson;

public class AuthRouteTest extends BaseServletTest {

    private static final GalasaGson gson = new GalasaGson();

    private String buildRequestPayload(String clientId, String refreshToken, String authCode) {
        JsonObject requestPayload = new JsonObject();
        if (clientId != null) {
            requestPayload.addProperty("client_id", clientId);
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

    private String buildTokenResponse(String jwt, String refreshToken) {
        JsonObject responseJson = new JsonObject();

        responseJson.addProperty("id_token", jwt);
        responseJson.addProperty("refresh_token", refreshToken);

        return gson.toJson(responseJson);
    }

    @Test
    public void testAuthPostRequestWithEmptyRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

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
        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        // Payload with a missing "refresh_token" field
        String payload = buildRequestPayload("dummy-id", null, null);
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
        String dummyJwt = "this-is-a-jwt";
        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret";
        String refreshToken = "here-is-a-token";

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(clientId, clientSecret, refreshToken)).thenReturn(mockResponse);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer", clientId, clientSecret, "http://callback");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        String payload = buildRequestPayload(clientId, refreshToken, null);
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
        //   "jwt": "this-is-a-jwt",
        //   "refresh_token": "this-is-a-refresh-token",
        // }
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("jwt", dummyJwt);
        expectedJsonObject.addProperty("refresh_token", dummyRefreshToken);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJsonObject));
    }

    @Test
    public void testAuthPostRequestWithValidAuthCodeRequestPayloadReturnsJWT() throws Exception {
        // Given...
        String dummyJwt = "this-is-a-jwt";
        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret";
        String authCode = "thisisacode";

        String callbackUri = "http://api.host/auth/callback";
        String issuerUrl = "http://dummy.host";

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(clientId, clientSecret, authCode, callbackUri)).thenReturn(mockResponse);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient(issuerUrl, clientId, clientSecret, callbackUri);
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        String payload = buildRequestPayload(clientId, null, authCode);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        mockEnv.setenv(EnvironmentVariables.GALASA_DEX_ISSUER, issuerUrl);
        mockEnv.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, "http://api.host");

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "jwt": "this-is-a-jwt",
        //   "refresh_token": "this-is-a-refresh-token",
        // }
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("jwt", dummyJwt);
        expectedJsonObject.addProperty("refresh_token", dummyRefreshToken);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJsonObject));
    }

    @Test
    public void testAuthPostRequestThrowsUnexpectedErrorReturnsServerError() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendTokenPost(any(), any(), any())).thenThrow(new IOException("simulating an unexpected failure!"));

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        String payload = buildRequestPayload("dummy-id", "here-is-a-token", null);
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

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        String payload = buildRequestPayload("dummy-id", "here-is-a-token", null);
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
    public void testAuthGetRequestWithClientIdAndCallbackUrlRedirectsToConnector() throws Exception {
        // Given...
        String redirectLocation = "http://my.connector/auth";
        String clientId = "my-client";
        String clientCallbackUrl = "http://my.app";

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.getConnectorRedirectUrl(eq(clientId), any(), any())).thenReturn(redirectLocation);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        Map<String, String[]> queryParams = Map.of(
                "client_id", new String[] { clientId }, "callback_url", new String[] { clientCallbackUrl }
        );

        MockHttpSession mockSession = new MockHttpSession();

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null, mockSession);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(302);
        assertThat(servletResponse.getHeader("Location")).isEqualTo(redirectLocation);
        assertThat((String)mockSession.getAttribute("callbackUrl")).isEqualTo(clientCallbackUrl);
    }

    @Test
    public void testAuthGetRequestWithEmptyReturnedLocationHeaderReturnsError() throws Exception {
        // Given...
        // No "Location" returned from the issuer, will not be able to redirect anywhere to authenticate
        Map<String, List<String>> headers = new HashMap<>();
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<String> mockResponse = new MockHttpResponse<String>("", HttpHeaders.of(headers, defaultFilter));

        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        when(mockOidcProvider.sendAuthorizationGet(any(), any(), any())).thenReturn(mockResponse);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        Map<String, String[]> queryParams = Map.of(
                "client_id", new String[] { "my-client" }, "callback_url", new String[] { "http://my.app" }
        );

        MockHttpSession mockSession = new MockHttpSession();

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null, mockSession);
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

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        Map<String, String[]> queryParams = Map.of("callbackUrl", new String[] { "http://my.callback.url" });
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

    @Test
    public void testAuthGetRequestWithMissingCallbackUrlReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        Map<String, String[]> queryParams = Map.of("client_id", new String[] { "my-client-id" });
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

    @Test
    public void testAuthGetRequestWithMissingParamsReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

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

    @Test
    public void testAuthGetRequestWithBadCallbackUrlReturnsBadRequest() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();
        setRequiredEnvironmentVariables(mockEnv);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        Map<String, String[]> queryParams = Map.of(
            "client_id", new String[] { "my-client-id" },
            "callback_url", new String[] { "!!!not-a-valid-url?!" }
        );
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

    @Test
    public void testAuthServletInitWithMissingRequiredEnvVarsThrowsServletException() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        // When...
        Throwable thrown = catchThrowable(() -> {
            servlet.init();
        });

        // Then...
        assertThat(thrown).isInstanceOf(ServletException.class);
    }
}