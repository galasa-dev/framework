package io.ejat.framework.spi.creds;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.IFrameworkInitialisation;

public interface ICredentialsStoreRegistration {

    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsException;
}