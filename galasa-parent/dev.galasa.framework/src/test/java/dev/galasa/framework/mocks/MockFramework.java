/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import dev.galasa.framework.Framework;
import dev.galasa.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.galasa.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

import java.io.File;
import java.util.Properties;
import javax.validation.constraints.NotNull;

public class MockFramework extends Framework {

    private Properties overrides = new Properties();
    private Properties records = new Properties();
    private File cps;

    public MockFramework() {
        super();
    }

    public MockFramework(File cpsFile) {
        this.cps = cpsFile;
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace) throws ConfigurationPropertyStoreException {
        IConfigurationPropertyStoreService cpsService = null;
        if (this.cps != null) {
            try {
                cpsService = new FrameworkConfigurationPropertyService(this,
                        new FpfConfigurationPropertyStore(cps.toURI()), overrides, records, namespace);
            } catch (Exception e) {
                throw new ConfigurationPropertyStoreException("error initialising", e);
            }
        } else {
            cpsService = super.getConfigurationPropertyService(namespace);
        }
        return cpsService;
    }
}