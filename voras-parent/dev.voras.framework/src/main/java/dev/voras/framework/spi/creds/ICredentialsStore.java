package dev.voras.framework.spi.creds;

import dev.voras.ICredentials;

public interface ICredentialsStore {

    ICredentials getCredentials(String credsId) throws CredentialsException;
}