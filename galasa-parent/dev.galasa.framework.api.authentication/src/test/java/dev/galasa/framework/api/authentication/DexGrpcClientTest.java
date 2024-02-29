/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.coreos.dex.api.DexOuterClass.Client;

import dev.galasa.framework.api.authentication.mocks.MockDexGrpcClient;

public class DexGrpcClientTest {

    @Test
    public void testCreateClientWithValidClientResponseCreatesDexClient() throws Exception {
        // Given...
        String callbackUrl = "http://my-app/callback";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", "client-id", "secret", callbackUrl);

        // When...
        Client responseClient = client.createClient(callbackUrl);

        // Then...
        assertThat(responseClient).isEqualTo(client.getDexClient());
    }

    @Test
    public void testCreateClientWithEmptyClientResponseReturnsNull() throws Exception {
        // Given...
        String callbackUrl = "http://my-app/callback";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer");

        // When...
        Client responseClient = client.createClient(callbackUrl);

        // Then...
        assertThat(responseClient).isNull();
    }

    @Test
    public void testGetClientWithEmptyClientResponseReturnsNull() throws Exception {
        // Given...
        String clientId = "myclient";

        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer");

        // When...
        Client responseClient = client.getClient(clientId);

        // Then...
        assertThat(responseClient).isNull();
    }

    @Test
    public void testGetClientWithValidClientResponseReturnsClient() throws Exception {
        // Given...
        String clientId = "myclient";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", clientId, "secret", "http://callback");

        // When...
        Client responseClient = client.getClient(clientId);

        // Then...
        assertThat(responseClient).isEqualTo(client.getDexClient());
    }
}
