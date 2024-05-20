/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import dev.galasa.framework.spi.Api;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.auth.IUserStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

import java.net.URL;
import java.util.Properties;
import java.util.Random;
import javax.validation.constraints.NotNull;

public class MockFramework implements IFramework {
    IResultArchiveStore archiveStore;
    IFrameworkRuns frameworkRuns;
    MockConfigurationPropertyStoreService cpsService = new MockConfigurationPropertyStoreService("framework");
    IUserStoreService userStoreService;

    public MockFramework() {
        // Do nothing...
    }

    public MockFramework(IUserStoreService userStoreService) {
        this.userStoreService = userStoreService;
    }

    public MockFramework(IResultArchiveStore archiveStore) {
        this.archiveStore = archiveStore;
    }

    public MockFramework(IFrameworkRuns frameworkRuns){
        this.frameworkRuns = frameworkRuns;
    }

    public MockFramework(IResultArchiveStore archiveStore, IFrameworkRuns frameworkRuns) {
        this.archiveStore = archiveStore;
        this.frameworkRuns = frameworkRuns;
    }

    public MockFramework(IConfigurationPropertyStoreService cpsService){
        this.cpsService = (MockConfigurationPropertyStoreService) cpsService;
    }

    @Override
    public @NotNull IUserStoreService getUserStoreService() {
        return this.userStoreService;
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
            if(this.cpsService.namespaceInput.equalsIgnoreCase("error")){
                throw new ConfigurationPropertyStoreException();
            }
       return this.cpsService;
    }

    @Override
    public @NotNull IResultArchiveStore getResultArchiveStore() {
        return archiveStore;
    }

    @Override
    public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
        return this.frameworkRuns;
    }

    @Override
    public void setFrameworkProperties(Properties overrideProperties) {
        throw new UnsupportedOperationException("Unimplemented method 'setFrameworkProperties'");
    }

    @Override
    public boolean isInitialised() {
        throw new UnsupportedOperationException("Unimplemented method 'isInitialised'");
    }

    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
            throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getDynamicStatusStoreService'");
    }

    @Override
    public @NotNull ICertificateStoreService getCertificateStoreService() {
        throw new UnsupportedOperationException("Unimplemented method 'getCertificateStoreService'");
    }

    @Override
    public @NotNull IResourcePoolingService getResourcePoolingService() {
        throw new UnsupportedOperationException("Unimplemented method 'getResourcePoolingService'");
    }

    @Override
    public @NotNull IConfidentialTextService getConfidentialTextService() {
        throw new UnsupportedOperationException("Unimplemented method 'getConfidentialTextService'");
    }

    @Override
    public @NotNull ICredentialsService getCredentialsService() throws CredentialsException {
        throw new UnsupportedOperationException("Unimplemented method 'getCredentialsService'");
    }

    @Override
    public String getTestRunName() {
        throw new UnsupportedOperationException("Unimplemented method 'getTestRunName'");
    }

    @Override
    public Random getRandom() {
        throw new UnsupportedOperationException("Unimplemented method 'getRandom'");
    }

    @Override
    public IRun getTestRun() {
        throw new UnsupportedOperationException("Unimplemented method 'getTestRun'");
    }

    @Override
    public Properties getRecordProperties() {
        throw new UnsupportedOperationException("Unimplemented method 'getRecordProperties'");
    }

    @Override
    public URL getApiUrl(@NotNull Api api) throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getApiUrl'");
    }

    @Override
    public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getSharedEnvironmentRunType'");
    }
}