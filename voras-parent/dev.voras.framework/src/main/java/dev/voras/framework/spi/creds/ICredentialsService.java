package dev.voras.framework.spi.creds;

import javax.validation.constraints.NotNull;

public interface ICredentialsService {

    ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException;
}