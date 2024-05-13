/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.List;

import dev.galasa.framework.spi.auth.AuthToken;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.User;
import dev.galasa.framework.spi.auth.AuthStoreException;

public class MockAuthStore implements IAuthStore, IAuthStoreService {

    @Override
    public List<AuthToken> getTokens() throws AuthStoreException {
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
}
