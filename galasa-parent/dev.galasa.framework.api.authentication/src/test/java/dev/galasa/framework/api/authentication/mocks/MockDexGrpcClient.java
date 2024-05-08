package dev.galasa.framework.api.authentication.mocks;

import com.coreos.dex.api.DexOuterClass.Client;
import com.coreos.dex.api.DexOuterClass.CreateClientReq;
import com.coreos.dex.api.DexOuterClass.CreateClientResp;
import com.coreos.dex.api.DexOuterClass.GetClientReq;
import com.coreos.dex.api.DexOuterClass.GetClientResp;
import com.coreos.dex.api.DexOuterClass.CreateClientResp.Builder;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

// A class that only mocks out methods that interact with Dex's gRPC service
public class MockDexGrpcClient extends DexGrpcClient {

    private Client dexClient;

    public MockDexGrpcClient(String issuerHostname, String clientId, String clientSecret, String callbackUrl) {
        super(issuerHostname, "http://my-ecosystem");
        this.dexClient = createDexClient(clientId, clientSecret, callbackUrl);
    }

    public MockDexGrpcClient(String issuerHostname) {
        super(issuerHostname, "http://my-ecosystem");
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
        if (this.dexClient != null) {
            createClientRespBuilder.setClient(this.dexClient);
        }
        return createClientRespBuilder.build();
    }

    @Override
    public GetClientResp sendGetClientRequest(GetClientReq getClientReq) {
        com.coreos.dex.api.DexOuterClass.GetClientResp.Builder getClientRespBuilder = GetClientResp.newBuilder();
        if (this.dexClient != null) {
            if (this.dexClient.getId().equals("error")) {
                throw new StatusRuntimeException(Status.UNKNOWN);
            } else {
                getClientRespBuilder.setClient(this.dexClient);
            }
        }
        return getClientRespBuilder.build();
    }

    private Client createDexClient(String clientId, String clientSecret, String callbackUrl) {
        com.coreos.dex.api.DexOuterClass.Client.Builder clientBuilder = Client.newBuilder();
        clientBuilder.setId(clientId);
        clientBuilder.setSecret(clientSecret);
        clientBuilder.addRedirectUris(callbackUrl);

        return clientBuilder.build();
    }

    public Client getDexClient() {
        return dexClient;
    }
}