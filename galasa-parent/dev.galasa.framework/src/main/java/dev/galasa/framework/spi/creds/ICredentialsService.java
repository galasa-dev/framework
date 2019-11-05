/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.creds;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;

public interface ICredentialsService {

    ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException;
}
