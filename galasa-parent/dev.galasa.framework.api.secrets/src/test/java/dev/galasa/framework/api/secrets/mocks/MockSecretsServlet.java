/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.mocks;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.secrets.SecretsServlet;

public class MockSecretsServlet extends SecretsServlet {

    public MockSecretsServlet(MockFramework framework) {
        this(framework, new MockEnvironment());
    }

    public MockSecretsServlet(MockFramework framework, Environment env) {
        this.framework = framework;
        setResponseBuilder(new ResponseBuilder(env));
    }
}
