package dev.voras.framework.spi.creds;

import javax.validation.constraints.NotNull;

import dev.voras.framework.spi.IFrameworkInitialisation;

public interface ICredentialsStoreRegistration {

    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsException;
}