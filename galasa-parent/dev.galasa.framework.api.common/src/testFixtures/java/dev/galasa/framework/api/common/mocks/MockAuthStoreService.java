/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.User;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.auth.AuthStoreException;

public class MockAuthStoreService implements IAuthStoreService {

    List<IInternalAuthToken> tokens = new ArrayList<>();
    private ITimeService timeService;
    private int tokenIdCounter = 0;

    private boolean throwException = false;

    public MockAuthStoreService(List<IInternalAuthToken> tokens) {
        this.tokens = tokens;
        this.timeService = new MockTimeService(Instant.now());
    }

    public MockAuthStoreService(ITimeService timeService) {
        this.timeService = timeService;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    @Override
    public List<IInternalAuthToken> getTokens() throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }
        return tokens;
    }

    @Override
    public void storeToken(String clientId, String description, User owner) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }
        tokens.add(new MockInternalAuthToken("token-" + tokenIdCounter, description, timeService.now(), owner, clientId));
        tokenIdCounter++;
    }

    private void throwAuthStoreException() throws AuthStoreException {
        throw new AuthStoreException("simulating an unexpected failure!");
    }

    @Override
    public void deleteToken(String tokenId) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }

        IInternalAuthToken tokenToRemove = getToken(tokenId);
        if (tokenToRemove != null) {
            tokens.remove(tokenToRemove);
        } else {
            throw new AuthStoreException("did not find token to delete!");
        }
    }

    @Override
    public IInternalAuthToken getToken(String tokenId) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }

        IInternalAuthToken tokenToReturn = null;
        for (IInternalAuthToken token : tokens) {
            if (token.getTokenId().equals(tokenId)) {
                tokenToReturn = token;
                break;
            }
        }
        return tokenToReturn;
    }
}
