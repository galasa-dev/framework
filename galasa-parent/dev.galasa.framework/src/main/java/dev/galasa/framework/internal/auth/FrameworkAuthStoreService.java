/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.auth;

import java.util.List;

import dev.galasa.framework.spi.auth.AuthToken;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.AuthStoreException;

/**
 * An implementation of the auth store service which is used to retrieve user
 * and other auth-related information within the auth store.
 */
public class FrameworkAuthStoreService implements IAuthStoreService {
    private final IAuthStore authStore;

    public FrameworkAuthStoreService(IAuthStore authStore) {
        this.authStore = authStore;
    }

    @Override
    public List<AuthToken> getTokens() throws AuthStoreException {
        return authStore.getTokens();
    }
}
