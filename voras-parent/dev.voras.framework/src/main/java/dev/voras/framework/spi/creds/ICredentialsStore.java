package dev.voras.framework.spi.creds;

public interface ICredentialsStore {

    ICredentials getCredentials(String credsId) throws CredentialsException;
}