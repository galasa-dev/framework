package io.ejat.framework.spi.creds;

import io.ejat.framework.spi.IFrameworkInitialisation;

import javax.crypto.IllegalBlockSizeException;

import javax.validation.constraints.NotNull;

public interface ICredentialsStoreService {

    ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsStoreException, IllegalBlockSizeException;
}