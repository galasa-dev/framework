/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.coreos.dex.api.DexGrpc;
import com.coreos.dex.api.DexGrpc.DexBlockingStub;
import com.coreos.dex.api.DexOuterClass.Client;
import com.coreos.dex.api.DexOuterClass.CreateClientReq;
import com.coreos.dex.api.DexOuterClass.CreateClientResp;
import com.coreos.dex.api.DexOuterClass.DeleteClientReq;
import com.coreos.dex.api.DexOuterClass.DeleteClientResp;
import com.coreos.dex.api.DexOuterClass.GetClientReq;
import com.coreos.dex.api.DexOuterClass.GetClientResp;
import com.coreos.dex.api.DexOuterClass.CreateClientReq.Builder;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

/**
 * A gRPC client class that interacts with Dex's API over gRPC, using the
 * auto-generated Dex gRPC code
 */
public class DexGrpcClient {

    private final Log logger = LogFactory.getLog(getClass());

    private DexBlockingStub blockingStub;

    private String externalWebUiUrl;

    public DexGrpcClient(String issuerHostname, String externalWebUiUrl) {
        initialiseBlockingStub(issuerHostname);
        this.externalWebUiUrl = externalWebUiUrl;
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
     * Returns a Dex client with a given client ID.
     *
     * @param clientId the ID of the client to retrieve
     * @throws InternalServletException if there was an issue retrieving the client
     */
    public Client getClient(String clientId) throws InternalServletException {
        logger.info("Retrieving Dex client with ID: " + clientId);

        // Build the GetClient request
        com.coreos.dex.api.DexOuterClass.GetClientReq.Builder getClientReqBuilder = GetClientReq.newBuilder();
        getClientReqBuilder.setId(clientId);

        Client client = null;
        try {
            // Send the gRPC call to get the Dex client
            GetClientReq getClientReq = getClientReqBuilder.build();
            GetClientResp clientResp = sendGetClientRequest(getClientReq);

            if (clientResp.hasClient()) {
                logger.info("Dex client successfully retrieved");
                client = clientResp.getClient();
            } else {
                // Something went wrong, the gRPC response didn't contain a Dex client
                ServletError error = new ServletError(GAL5052_FAILED_TO_RETRIEVE_CLIENT, externalWebUiUrl);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (StatusRuntimeException ex) {
            // A StatusRuntimeException is thrown if no such client exists with the given ID,
            // so consider this a bad request
            ServletError error = new ServletError(GAL5051_INVALID_GALASA_TOKEN_PROVIDED, externalWebUiUrl);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, ex);
        }
        return client;
    }

    /**
     * Deletes a Dex client with a given client ID.
     *
     * @param clientId the ID of the client to delete
     * @throws InternalServletException if there was an issue deleting the client
     */
    public void deleteClient(String clientId) throws InternalServletException {
        logger.info("Deleting Dex client with ID: " + clientId);

        // Build the DeleteClient request
        com.coreos.dex.api.DexOuterClass.DeleteClientReq.Builder deleteClientReqBuilder = DeleteClientReq.newBuilder();
        deleteClientReqBuilder.setId(clientId);

        // Send the gRPC call to delete the Dex client
        DeleteClientReq deleteClientReq = deleteClientReqBuilder.build();
        DeleteClientResp clientResp = sendDeleteClientRequest(deleteClientReq);

        if (!clientResp.getNotFound()) {
            logger.info("Dex client successfully deleted");
        } else {
            // Something went wrong, the client with the given ID couldn't be found
            ServletError error = new ServletError(GAL5063_FAILED_TO_DELETE_CLIENT);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
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

    /**
     * Sends a request to delete a Dex client.
     *
     * @param deleteClientReq the request to send
     * @return the response received from Dex's gRPC API
     */
    protected DeleteClientResp sendDeleteClientRequest(DeleteClientReq deleteClientReq) {
        return blockingStub.deleteClient(deleteClientReq);
    }
}
