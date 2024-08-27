/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.AbstractTestRunner;
import dev.galasa.framework.BundleManagement;
import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.spi.FrameworkException;

public class BundleManager implements IBundleManager {

    private Log logger = LogFactory.getLog(BundleManager.class);

    @Override
    public boolean isBundleActive(BundleContext bundleContext, String bundleSymbolicName) {
        return BundleManagement.isBundleActive(bundleContext, bundleSymbolicName);
    }

    @Override
    public void loadAllGherkinManagerBundles(RepositoryAdmin repositoryAdmin, BundleContext bundleContext)
            throws FrameworkException {
        BundleManagement.loadAllGherkinManagerBundles(repositoryAdmin, bundleContext);
    }

    @Override
    public void loadBundle(RepositoryAdmin repositoryAdmin, BundleContext bundleContext, String bundleSymbolicName)
            throws FrameworkException {
        try {
            BundleManagement.loadBundle(repositoryAdmin, bundleContext, bundleSymbolicName);
        }
    }
    
}
