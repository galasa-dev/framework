/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal;

import java.util.Properties;

import dev.galasa.ICredentials;

class MockCredentials implements ICredentials {

    @Override
    public Properties toProperties(String credentialsId) {
        throw new UnsupportedOperationException("Unimplemented method 'toProperties'");
    }
}
