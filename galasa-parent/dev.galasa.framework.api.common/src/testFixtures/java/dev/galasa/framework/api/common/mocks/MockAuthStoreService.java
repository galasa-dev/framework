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
import dev.galasa.framework.spi.auth.IAuthToken;
import dev.galasa.framework.spi.auth.User;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.AuthToken;

public class MockAuthStoreService implements IAuthStoreService {

    List<IAuthToken> tokens = new ArrayList<>();
    private ITimeService timeService;
    private int tokenIdCounter = 0;

    private boolean throwException = false;

    public MockAuthStoreService(List<IAuthToken> tokens) {
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
    public List<IAuthToken> getTokens() throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }

        return tokens;
    }

    private void throwAuthStoreException() throws AuthStoreException {
        throw new AuthStoreException("simulating an unexpected failure!");
    }

    @Override
    public void storeToken(String clientId, String description, User owner) throws AuthStoreException {
        tokens.add(new AuthToken("token-" + tokenIdCounter, description, timeService.now(), owner));
        tokenIdCounter++;
    }
}
