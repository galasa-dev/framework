/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.creds;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IFrameworkInitialisation;

public interface ICredentialsStoreRegistration {

    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsException;
}
