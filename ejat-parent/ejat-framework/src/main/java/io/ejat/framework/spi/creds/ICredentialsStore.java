package io.ejat.framework.spi;

public interface ICredentialsStore {

    ICredentials getCredentials(String credsId) throws CredentialsStoreException;
}