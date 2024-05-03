/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.List;

import dev.galasa.framework.spi.auth.AuthToken;
import dev.galasa.framework.spi.auth.IUserStore;
import dev.galasa.framework.spi.auth.IUserStoreService;
import dev.galasa.framework.spi.auth.UserStoreException;

public class MockUserStore implements IUserStore, IUserStoreService {

    @Override
    public List<AuthToken> getTokens() throws UserStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getTokens'");
    }

    @Override
    public void shutdown() throws UserStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }
}
