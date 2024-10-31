/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;

public interface ICredentialsService {

    /**
     * Gets the credentials with the given ID and returns them after attempting to decrypt their values 
     * 
     * @param credentialsId         the ID of the credentials to retrieve from the credentials store
     * @return                      the decrypted credentials in the credentials store, or null if no such credentials exist
     * @throws CredentialsException if there was an issue accessing the credentials store
     */
    ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException;

    /**
     * Gets all the credentials stored in the credentials store
     * 
     * @return                      a map of credentials, where keys correspond to credentials IDs and
     *                              values correspond to the credentials
     * @throws CredentialsException if there was an issue accessing the credentials store
     */
    Map<String, ICredentials> getAllCredentials() throws CredentialsException;

    void setCredentials(String credentialsId, ICredentials credentials) throws CredentialsException;

    void deleteCredentials(String credentialsId) throws CredentialsException;
}
