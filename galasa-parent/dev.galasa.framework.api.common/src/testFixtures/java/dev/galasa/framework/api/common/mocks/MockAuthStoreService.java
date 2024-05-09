/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.auth.AuthToken;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.AuthStoreException;

public class MockAuthStoreService implements IAuthStoreService {

    List<AuthToken> tokens = new ArrayList<>();
    private boolean throwException = false;

    public MockAuthStoreService(List<AuthToken> tokens) {
        this.tokens = tokens;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    @Override
    public List<AuthToken> getTokens() throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }

        return tokens;
    }

    private void throwAuthStoreException() throws AuthStoreException {
        throw new AuthStoreException("simulating an unexpected failure!");
    }
}
