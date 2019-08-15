package dev.galasa.framework.spi.creds;

import dev.galasa.ICredentials;

public interface ICredentialsStore {

    ICredentials getCredentials(String credsId) throws CredentialsException;
    
    void shutdown() throws CredentialsException;
}