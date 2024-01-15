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

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.coreos.dex.api.DexOuterClass.Client;
import com.coreos.dex.api.DexOuterClass.Client.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class AuthClientsRouteTest extends BaseServletTest {

    private static final Gson gson = GalasaGsonBuilder.build();

    private Client createMockClient(String clientId, String clientSecret, String redirectUri) {
        Builder clientBuilder = Client.newBuilder();

        clientBuilder.setId(clientId);
        clientBuilder.setSecret(clientSecret);
        clientBuilder.addRedirectUris(redirectUri);

        return clientBuilder.build();
    }

    @Test
    public void testAuthClientsPostRequestWithNoCreatedClientReturnsError() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        DexGrpcClient mockDexGrpcClient = mock(DexGrpcClient.class);
        when(mockDexGrpcClient.createClient(any())).thenReturn(null);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/clients", "", "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

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
    public void testAuthClientsPostRequestReturnsClient() throws Exception {
        // Given...
        OidcProvider mockOidcProvider = mock(OidcProvider.class);
        MockEnvironment mockEnv = new MockEnvironment();

        String clientId = "my-client-id";
        String clientSecret = "my-client-secret";
        String redirectUri = "http://my.app/callback";

        DexGrpcClient mockDexGrpcClient = mock(DexGrpcClient.class);
        when(mockDexGrpcClient.createClient(any())).thenReturn(createMockClient(clientId, clientSecret, redirectUri));

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/clients", "", "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "client_id": "my-client-id",
        //   "client_secret": "my-client-secret",
        // }
        JsonObject expectedJson = new JsonObject();
        expectedJson.addProperty("client_id", clientId);
        expectedJson.addProperty("client_secret", clientSecret);

        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJson));
    }
}
