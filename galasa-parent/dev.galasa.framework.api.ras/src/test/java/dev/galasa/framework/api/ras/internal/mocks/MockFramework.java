/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

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
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

import java.net.URL;
import java.util.Properties;
import java.util.Random;
import javax.validation.constraints.NotNull;

public class MockFramework implements IFramework {
    IResultArchiveStore archiveStore;

    public MockFramework(IResultArchiveStore archiveStore) {
        this.archiveStore = archiveStore;
    }

    @Override
    public void setFrameworkProperties(Properties overrideProperties) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setFrameworkProperties'");
    }

    @Override
    public boolean isInitialised() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isInitialised'");
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConfigurationPropertyService'");
    }

    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
            throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDynamicStatusStoreService'");
    }

    @Override
    public @NotNull ICertificateStoreService getCertificateStoreService() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCertificateStoreService'");
    }

    @Override
    public @NotNull IResultArchiveStore getResultArchiveStore() {
        return archiveStore;
    }

    @Override
    public @NotNull IResourcePoolingService getResourcePoolingService() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResourcePoolingService'");
    }

    @Override
    public @NotNull IConfidentialTextService getConfidentialTextService() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConfidentialTextService'");
    }

    @Override
    public @NotNull ICredentialsService getCredentialsService() throws CredentialsException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCredentialsService'");
    }

    @Override
    public String getTestRunName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTestRunName'");
    }

    @Override
    public Random getRandom() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRandom'");
    }

    @Override
    public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFrameworkRuns'");
    }

    @Override
    public IRun getTestRun() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTestRun'");
    }

    @Override
    public Properties getRecordProperties() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRecordProperties'");
    }

    @Override
    public URL getApiUrl(@NotNull Api api) throws FrameworkException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getApiUrl'");
    }

    @Override
    public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSharedEnvironmentRunType'");
    }
    
}