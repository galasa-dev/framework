/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.coreos.dex.api.DexOuterClass.Client;
import com.coreos.dex.api.DexOuterClass.CreateClientReq;
import com.coreos.dex.api.DexOuterClass.CreateClientResp;
import com.coreos.dex.api.DexOuterClass.CreateClientResp.Builder;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;

public class DexGrpcClientTest {

    class MockDexGrpcClient extends DexGrpcClient {

        private Client mockClient;

        public MockDexGrpcClient(String issuerHostname, Client mockClient) {
            super(issuerHostname);
            this.mockClient = mockClient;
        }

        // Mock out the creation of the blocking stub
        @Override
        public void initialiseBlockingStub(String issuerHostname) {
            // Do nothing...
        }

        // Mock out the response from Dex's gRPC API
        @Override
        public CreateClientResp sendCreateClientRequest(CreateClientReq createClientReq) {
            Builder createClientRespBuilder = CreateClientResp.newBuilder();
            if (this.mockClient != null) {
                createClientRespBuilder.setClient(this.mockClient);
            }
            return createClientRespBuilder.build();
        }
    }

    private Client createMockDexClient(String callbackUrl) {
        com.coreos.dex.api.DexOuterClass.Client.Builder clientBuilder = Client.newBuilder();
        clientBuilder.setId("dummy-id");
        clientBuilder.setSecret("dummy-secret");
        clientBuilder.addRedirectUris(callbackUrl);

        return clientBuilder.build();
    }

    @Test
    public void testCreateClientWithValidClientResponseCreatesDexClient() throws Exception {
        // Given...
        String callbackUrl = "http://my-app/callback";
        Client mockClient = createMockDexClient(callbackUrl);
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", mockClient);

        // When...
        Client responseClient = client.createClient(callbackUrl);

        // Then...
        assertThat(responseClient).isEqualTo(mockClient);
    }

    @Test
    public void testCreateClientWithEmptyClientResponseReturnsNull() throws Exception {
        // Given...
        String callbackUrl = "http://my-app/callback";
        Client mockClient = null;
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", mockClient);

        // When...
        Client responseClient = client.createClient(callbackUrl);

        // Then...
        assertThat(responseClient).isNull();
    }
}
