package io.ejat.framework.spi.creds;

public interface ICredentialsStore {

    ICredentials getCredentials(String credsId) throws CredentialsStoreException;
}