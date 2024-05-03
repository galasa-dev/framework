/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.auth.IUserStore;
import dev.galasa.framework.spi.auth.IUserStoreRegistration;
import dev.galasa.framework.spi.auth.UserStoreException;

public class MockUserStoreRegistration implements IUserStoreRegistration {

    private IUserStore store;

    public MockUserStoreRegistration(IUserStore store) {
        this.store = store;
    }

    @Override
    public void initialise(IFrameworkInitialisation frameworkInitialisation) throws UserStoreException {
        frameworkInitialisation.registerUserStore(store);
    }
}