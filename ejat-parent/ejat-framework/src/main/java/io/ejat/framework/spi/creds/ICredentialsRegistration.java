package io.ejat.framework.spi.creds;

import io.ejat.framework.spi.IFrameworkInitialisation;

import javax.validation.constraints.NotNull;

public interface ICredentialsRegistration {

    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsStoreException;
}