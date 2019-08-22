package dev.galasa.framework.spi.creds;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;

public interface ICredentialsService {

    ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException;
}