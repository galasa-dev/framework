/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.mocks;

import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockTimeService;
import dev.galasa.framework.api.secrets.SecretsServlet;
import dev.galasa.framework.spi.utils.ITimeService;

public class MockSecretsServlet extends SecretsServlet {

    public MockSecretsServlet(MockFramework framework, MockTimeService mockTimeService) {
        this(framework, new MockEnvironment(), mockTimeService);
    }

    public MockSecretsServlet(MockFramework framework, MockEnvironment env, ITimeService timeService) {
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        this.framework = framework;
        this.env = env;
        this.timeService = timeService;
        setResponseBuilder(new ResponseBuilder(env));
    }
}
