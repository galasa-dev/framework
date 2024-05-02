/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.auth.AuthToken;
import dev.galasa.framework.spi.auth.IUserStoreService;
import dev.galasa.framework.spi.auth.UserStoreException;

public class MockUserStoreService implements IUserStoreService {

    List<AuthToken> tokens = new ArrayList<>();

    public MockUserStoreService(List<AuthToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public List<AuthToken> getTokens() throws UserStoreException {
        return tokens;
    }
}
