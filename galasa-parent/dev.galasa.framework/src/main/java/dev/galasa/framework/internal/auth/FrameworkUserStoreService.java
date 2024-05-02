/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.auth;

import java.util.List;

import dev.galasa.framework.spi.auth.AuthToken;
import dev.galasa.framework.spi.auth.IUserStore;
import dev.galasa.framework.spi.auth.IUserStoreService;
import dev.galasa.framework.spi.auth.UserStoreException;

/**
 * An implementation of the user store service which is used to retrieve user
 * and other auth-related information within the user store.
 */
public class FrameworkUserStoreService implements IUserStoreService {
    private final IUserStore userStore;

    public FrameworkUserStoreService(IUserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public List<AuthToken> getTokens() throws UserStoreException {
        return userStore.getTokens();
    }
}
