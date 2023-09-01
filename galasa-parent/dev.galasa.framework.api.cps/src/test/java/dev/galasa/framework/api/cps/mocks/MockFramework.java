/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.mocks;

import dev.galasa.framework.spi.*;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import java.net.URL;
import java.util.*;

public class MockFramework implements IFramework {

    @Override
    public void setFrameworkProperties(Properties overrideProperties) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isInitialised() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public IDynamicStatusStoreService getDynamicStatusStoreService(String namespace) throws DynamicStatusStoreException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public ICertificateStoreService getCertificateStoreService() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public IResultArchiveStore getResultArchiveStore() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public IResourcePoolingService getResourcePoolingService() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public IConfidentialTextService getConfidentialTextService() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public ICredentialsService getCredentialsService() throws CredentialsException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getTestRunName() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Random getRandom() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public IRun getTestRun() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Properties getRecordProperties() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public URL getApiUrl(Api api) throws FrameworkException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
        throw new MockMethodNotImplementedException();
    }
}