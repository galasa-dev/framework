/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import java.util.List;

public interface IAuthStore {

    /**
     * Returns a list of all the token records stored in the auth store.
     *
     * @return a list of all token records stored in the auth store.
     * @throws AuthStoreException if there is an issue accessing the auth store.
     */
    List<IInternalAuthToken> getTokens() throws AuthStoreException;

    /**
     * Stores a new token record in the auth store's tokens database.
     *
     * @param clientId    the ID of the Dex client that the token works with.
     * @param description the user-provided description of the token.
     * @param owner       the details of the user that the token belongs to.
     * @throws AuthStoreException if there is an issue accessing the auth store.
     */
    void storeToken(String clientId, String description, IInternalUser owner) throws AuthStoreException;

    /**
     * Deletes an existing token record from the auth store's tokens database.
     * 
     * @param tokenId the ID of the token record to delete.
     * @throws AuthStoreException if there was an issue accessing the auth store.
     */
    void deleteToken(String tokenId) throws AuthStoreException;

    void shutdown() throws AuthStoreException;
}
