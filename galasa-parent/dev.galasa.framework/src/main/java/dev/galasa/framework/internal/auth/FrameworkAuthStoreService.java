/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.auth;

import java.util.List;

import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;
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
    public List<IInternalAuthToken> getTokens() throws AuthStoreException {
        return authStore.getTokens();
    }

    @Override
    public IInternalAuthToken getToken(String tokenId) throws AuthStoreException {
        List<IInternalAuthToken> tokens = authStore.getTokens();

        IInternalAuthToken tokenToReturn = null;
        for (IInternalAuthToken token : tokens) {
            if (token.getTokenId().equals(tokenId)) {
                tokenToReturn = token;
                break;
            }
        }
        return tokenToReturn;
    }

    @Override
    public void storeToken(String clientId, String description, IInternalUser owner) throws AuthStoreException {
        authStore.storeToken(clientId, description, owner);
    }

    @Override
    public void deleteToken(String tokenId) throws AuthStoreException {
        authStore.deleteToken(tokenId);
    }

    @Override
    public List<IInternalAuthToken> getTokensByLoginId(String loginId) throws AuthStoreException {
        return authStore.getTokensByLoginId(loginId);
    }
}
