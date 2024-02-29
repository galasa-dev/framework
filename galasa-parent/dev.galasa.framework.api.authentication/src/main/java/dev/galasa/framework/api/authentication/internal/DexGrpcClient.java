/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.coreos.dex.api.DexGrpc;
import com.coreos.dex.api.DexGrpc.DexBlockingStub;
import com.coreos.dex.api.DexOuterClass.Client;
import com.coreos.dex.api.DexOuterClass.CreateClientReq;
import com.coreos.dex.api.DexOuterClass.CreateClientResp;
import com.coreos.dex.api.DexOuterClass.GetClientReq;
import com.coreos.dex.api.DexOuterClass.GetClientResp;
import com.coreos.dex.api.DexOuterClass.CreateClientReq.Builder;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * A gRPC client class that interacts with Dex's API over gRPC, using the
 * auto-generated Dex gRPC code
 */
public class DexGrpcClient {

    private final Log logger = LogFactory.getLog(getClass());

    private DexBlockingStub blockingStub;

    public DexGrpcClient(String issuerHostname) {
        initialiseBlockingStub(issuerHostname);
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

        // Send the gRPC call to create the new Dex client
        logger.info("Creating new Dex client");
        CreateClientResp clientResp = sendCreateClientRequest(createClientReq);

        Client createdClient = null;
        if (clientResp.hasClient()) {
            logger.info("Dex client successfully created");
            createdClient = clientResp.getClient();
        } else {
            logger.error("Failed to create new Dex client");
        }
        return createdClient;
    }

    /**
     * Returns a Dex client with a given client ID, or null if no such client exists.
     *
     * @param clientId the ID of the client to retrieve
     */
    public Client getClient(String clientId) {
        logger.info("Retrieving Dex client with ID: " + clientId);

        // Build the GetClient request
        com.coreos.dex.api.DexOuterClass.GetClientReq.Builder getClientReqBuilder = GetClientReq.newBuilder();
        getClientReqBuilder.setId(clientId);

        // Send the gRPC call to get the Dex client
        GetClientReq getClientReq = getClientReqBuilder.build();
        GetClientResp clientResp = sendGetClientRequest(getClientReq);

        Client client = null;
        if (clientResp.hasClient()) {
            logger.info("Dex client successfully retrieved");
            client = clientResp.getClient();
        } else {
            logger.error("Failed to get the Dex client with ID: " + clientId);
        }
        return client;
    }

    /**
     * Initialises a blocking stub to be used when sending requests to Dex's gRPC
     * API.
     *
     * @param issuerHostname the hostname of the Dex issuer to send gRPC requests to
     */
    protected void initialiseBlockingStub(String issuerHostname) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(issuerHostname).usePlaintext().build();
        this.blockingStub = DexGrpc.newBlockingStub(channel);
    }

    /**
     * Sends a request to create a new Dex client.
     *
     * @param createClientReq the request to send
     * @return the response received from Dex's gRPC API
     */
    protected CreateClientResp sendCreateClientRequest(CreateClientReq createClientReq) {
        return blockingStub.createClient(createClientReq);
    }

    /**
     * Sends a request to get a Dex client.
     *
     * @param getClientReq the request to send
     * @return the response received from Dex's gRPC API
     */
    protected GetClientResp sendGetClientRequest(GetClientReq getClientReq) {
        return blockingStub.getClient(getClientReq);
    }
}
