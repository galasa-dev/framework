/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreRegistration;
import dev.galasa.framework.spi.auth.AuthStoreException;

public class MockAuthStoreRegistration implements IAuthStoreRegistration {

    private IAuthStore store;

    public MockAuthStoreRegistration(IAuthStore store) {
        this.store = store;
    }

    @Override
    public void initialise(IFrameworkInitialisation frameworkInitialisation) throws AuthStoreException {
        frameworkInitialisation.registerAuthStore(store);
    }
}