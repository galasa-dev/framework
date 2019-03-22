package io.ejat.framework.spi;

import javax.validation.constraints.NotNull;

public interface ICredentialsStoreService {
    
    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsStoreException;

    ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsStoreException;
}