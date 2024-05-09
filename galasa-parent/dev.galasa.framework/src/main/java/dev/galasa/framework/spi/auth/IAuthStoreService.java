/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import java.util.List;

public interface IAuthStoreService {

    /**
     * Returns a list of all the tokens stored in the authentication store.
     *
     * @return a list of all tokens stored in the authentication store.
     * @throws AuthStoreException if there is an issue accessing the authentication
     *                            store.
     */
    List<AuthToken> getTokens() throws AuthStoreException;
}
