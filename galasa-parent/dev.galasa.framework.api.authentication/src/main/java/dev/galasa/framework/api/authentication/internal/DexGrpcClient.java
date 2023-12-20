/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import com.coreos.dex.api.DexGrpc;
import com.coreos.dex.api.DexGrpc.DexBlockingStub;
import com.coreos.dex.api.DexOuterClass.Client;
import com.coreos.dex.api.DexOuterClass.CreateClientReq;
import com.coreos.dex.api.DexOuterClass.CreateClientResp;
import com.coreos.dex.api.DexOuterClass.CreateClientReq.Builder;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * A gRPC client class that interacts with Dex's API over gRPC, using the
 * auto-generated Dex gRPC code
 */
public class DexGrpcClient {

    private String issuerHostname;

    public DexGrpcClient(String issuerHostname) {
        this.issuerHostname = issuerHostname;
    }

    /**
     * Creates a new Dex client using Dex's gRPC API.
     *
     * @param callbackUrl the callback URL used when authenticating with the new client
     */
    public Client createClient(String callbackUrl) {
        Builder createClientReqBuilder = CreateClientReq.newBuilder();
        com.coreos.dex.api.DexOuterClass.Client.Builder clientBuilder = Client.newBuilder();

        // Add a redirect URI to the new client, allow Dex to create the client ID and secret
        clientBuilder.addRedirectUris(callbackUrl);
        Client client = clientBuilder.build();

        createClientReqBuilder.setClient(client);
        CreateClientReq createClientReq = createClientReqBuilder.build();

        // Create a channel and blocking stub, then send the request to create the new client
        ManagedChannel channel = ManagedChannelBuilder.forTarget(issuerHostname).usePlaintext().build();
        DexBlockingStub dexGrpc = DexGrpc.newBlockingStub(channel);
        CreateClientResp clientResp = dexGrpc.createClient(createClientReq);

        if (clientResp.hasClient()) {
            return clientResp.getClient();
        }
        return null;
    }
}
