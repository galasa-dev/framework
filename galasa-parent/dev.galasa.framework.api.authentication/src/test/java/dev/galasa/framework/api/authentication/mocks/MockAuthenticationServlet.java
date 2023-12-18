/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import dev.galasa.framework.api.authentication.AuthenticationServlet;
import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.Environment;

public class MockAuthenticationServlet extends AuthenticationServlet {

    public MockAuthenticationServlet(Environment env, OidcProvider oidcProvider) {
        this.env = env;
        this.oidcProvider = oidcProvider;
    }

    public void setOidcProvider(OidcProvider oidcProvider) {
        this.oidcProvider = oidcProvider;
    }
}
