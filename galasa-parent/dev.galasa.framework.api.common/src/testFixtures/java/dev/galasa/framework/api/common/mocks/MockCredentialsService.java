package dev.galasa.framework.api.common.mocks;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class MockCredentialsService implements ICredentialsService {

    Map<String, ICredentials> creds = new HashMap<>();

    public MockCredentialsService(Map<String, ICredentials> creds) {
        this.creds = creds;
    }

    @Override
    public ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException {
        return this.creds.get(credentialsId);
    }
    
}
