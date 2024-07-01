/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.mocks;

import java.util.List;

import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IAuthToken;
import dev.galasa.framework.spi.auth.User;
import dev.galasa.framework.spi.auth.AuthStoreException;

public class MockAuthStore implements IAuthStore, IAuthStoreService {

    @Override
    public List<IAuthToken> getTokens() throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getTokens'");
    }

    @Override
    public void shutdown() throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    @Override
    public void storeToken(String clientId, String description, User owner) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'storeToken'");
    }

    @Override
    public void deleteToken(String tokenId) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteToken'");
    }

    @Override
    public IAuthToken getToken(String tokenId) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getToken'");
    }
}
