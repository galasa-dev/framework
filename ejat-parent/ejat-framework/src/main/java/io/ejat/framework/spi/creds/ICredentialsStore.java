package io.ejat.framework.spi.creds;

import javax.crypto.IllegalBlockSizeException;

public interface ICredentialsStore {

    ICredentials getCredentials(String credsId) throws CredentialsStoreException, IllegalBlockSizeException;
}