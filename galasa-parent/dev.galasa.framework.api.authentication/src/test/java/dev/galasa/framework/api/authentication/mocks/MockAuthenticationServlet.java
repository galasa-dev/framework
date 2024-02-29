/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import dev.galasa.framework.api.authentication.AuthenticationServlet;
import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.Environment;

public class MockAuthenticationServlet extends AuthenticationServlet {

    public MockAuthenticationServlet(Environment env, OidcProvider oidcProvider, DexGrpcClient dexGrpcClient) {
        this(env, oidcProvider);
        this.dexGrpcClient = dexGrpcClient;
    }

    public MockAuthenticationServlet(Environment env, OidcProvider oidcProvider) {
        this.env = env;
        this.oidcProvider = oidcProvider;
    }

    @Override
    protected void initialiseDexClients(String dexIssuerUrl, String dexGrpcHostname) {
        // Do nothing...
    }
}
