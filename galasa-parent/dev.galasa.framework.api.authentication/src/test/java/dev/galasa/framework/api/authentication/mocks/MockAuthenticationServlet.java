/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import dev.galasa.framework.api.authentication.AuthenticationServlet;
import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.spi.IFramework;

public class MockAuthenticationServlet extends AuthenticationServlet {

    public MockAuthenticationServlet(Environment env, IOidcProvider oidcProvider, DexGrpcClient dexGrpcClient) {
        this(env, oidcProvider);
        this.dexGrpcClient = dexGrpcClient;
    }

    public MockAuthenticationServlet(Environment env, IOidcProvider oidcProvider) {
        this.env = env;
        this.oidcProvider = oidcProvider;
    }

    public MockAuthenticationServlet(Environment env, IFramework framework) {
        this.env = env;
        this.framework = framework;
    }

    @Override
    protected void initialiseDexClients(String dexIssuerUrl, String dexGrpcHostname, String externalWebUiUrl) {
        // Do nothing...
    }
}
