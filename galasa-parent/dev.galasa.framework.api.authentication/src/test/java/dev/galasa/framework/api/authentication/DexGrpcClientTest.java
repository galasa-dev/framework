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
import dev.galasa.framework.api.common.InternalServletException;

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
    public void testGetClientWithEmptyClientResponseThrowsInternalServletException() throws Exception {
        // Given...
        String clientId = "myclient";

        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer");

        // When...
        Throwable thrown = catchThrowable(() -> {
            client.getClient(clientId);
        });

        // Then...
        assertThat(thrown).isInstanceOf(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5052E", "Unable to retrieve client for authentication");
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

    @Test
    public void testGetClientWithInvalidClientIdThrowsInternalServletException() throws Exception {
        // Given...
        // Force the mock client to throw a StatusRuntimeException
        String clientId = "error";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", clientId, "secret", "http://callback");

        // When...
        Throwable thrown = catchThrowable(() -> {
            client.getClient(clientId);
        });

        // Then...
        assertThat(thrown).isInstanceOf(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5051E","Invalid GALASA_TOKEN value provided");
    }
}
