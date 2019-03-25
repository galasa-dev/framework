package io.ejat.framework;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.IConfidentialTextService;
import io.ejat.framework.spi.creds.ICredentialsStoreService;
import io.ejat.framework.internal.cps.FrameworkConfigurationPropertyStore;
import io.ejat.framework.internal.cts.FrameworkConfidentialTextService;
import io.ejat.framework.internal.dss.FrameworkDynamicStatusStore;
import io.ejat.framework.internal.creds.FrameworkCredentialsStore;
import io.ejat.framework.spi.ConfidentialTextException;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.FrameworkException;
import io.ejat.framework.spi.IConfigurationPropertyStore;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IResourcePoolingService;
import io.ejat.framework.spi.IResultArchiveStore;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ResultArchiveStoreException;

public class Framework implements IFramework {

    private static final Pattern               namespacePattern = Pattern.compile("[a-z0-9]+");

    private final Properties                   overrideProperties;
    private final Properties                   recordProperties;

    private IConfigurationPropertyStoreService cpsService;
    private IDynamicStatusStoreService         dssService;
    private IResultArchiveStoreService         rasService;
    private IConfidentialTextService           ctsService;
    private ICredentialsStoreService           credsService;             

    private IConfigurationPropertyStore        cpsFramework;

    protected Framework(Properties overrideProperties, Properties recordProperties) {
        this.overrideProperties = overrideProperties;
        this.recordProperties = recordProperties;
    }

    @Override
    public @NotNull IConfigurationPropertyStore getConfigurationPropertyStore(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
        if (this.cpsService == null) {
            throw new ConfigurationPropertyStoreException("The Configuration Property Store has not been initialised");
        }

        try {
            validateNamespace(namespace);
        } catch (FrameworkException e) {
            throw new ConfigurationPropertyStoreException("Unable to provide Configuration Property Store", e);
        }

        return new FrameworkConfigurationPropertyStore(this, this.cpsService, this.overrideProperties,
                this.recordProperties, namespace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IFramework#getDynamicStatusStore(java.lang.String)
     */
    @Override
    public @NotNull IDynamicStatusStore getDynamicStatusStore(@NotNull String namespace)
            throws DynamicStatusStoreException {
        if (this.dssService == null) {
            throw new DynamicStatusStoreException("The Dynamic Status Store has not been initialised");
        }

        try {
            validateNamespace(namespace);
        } catch (FrameworkException e) {
            throw new DynamicStatusStoreException("Unable to provide Dynamic Status Store", e);
        }

        return new FrameworkDynamicStatusStore(this, this.dssService, namespace);
    }

    /**
     * Validate the namespace adheres to naming standards
     *
     * @param namespace - the namespace to check
     * @throws ConfigurationPropertyStoreException - if the namespace does meet the
     *                                             standards
     */
    private void validateNamespace(String namespace) throws FrameworkException {
        if (namespace == null) {
            throw new FrameworkException("Namespace has not been provided");
        }

        final Matcher matcher = namespacePattern.matcher(namespace);
        if (!matcher.matches()) {
            throw new FrameworkException("Invalid namespace");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IFramework#getResultArchiveStore()
     */
    @Override
    public @NotNull IResultArchiveStore getResultArchiveStore() {
        return this.rasService;
    }
    
    protected IResultArchiveStoreService getResultArchiveStoreService() {
        return this.rasService;
    }


    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IFramework#getResourcePoolingService()
     */
    @Override
    public @NotNull IResourcePoolingService getResourcePoolingService() {
        throw new UnsupportedOperationException("RPS has not been implemented yet");
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IFramework#getConfidentialTextService()
     */
    @Override
    public @NotNull IConfidentialTextService getConfidentialTextService() {
        return this.ctsService;
    }

    @Override
    public @NotNull ICredentialsStoreService getCredentialsStoreService() {
        return this.credsService;
    }

    /**
     * Set the new Configuration Property Store Service
     *
     * @param cpsService - The new CPS
     * @throws ConfigurationPropertyStoreException - If a CPS has already be
     *                                             registered
     */
    protected void setConfigurationPropertyStoreService(@NotNull IConfigurationPropertyStoreService cpsService)
            throws ConfigurationPropertyStoreException {
        if (this.cpsService != null) {
            throw new ConfigurationPropertyStoreException(
                    "Invalid 2nd registration of the Config Property Store Service detected");
        }

        this.cpsService = cpsService;
        this.cpsFramework = getConfigurationPropertyStore("framework");
    }

    public void setDynamicStatusStoreService(@NotNull IDynamicStatusStoreService dssService)
            throws DynamicStatusStoreException {
        if (this.dssService != null) {
            throw new DynamicStatusStoreException(
                    "Invalid 2nd registration of the Dynamic Status Store Service detected");
        }

        this.dssService = dssService;
    }

    /**
     * Add a new Result Archive Store Service to the framework, eventually we will
     * have the ability to have multiples RASs running
     *
     * @param resultArchiveStoreService - a new result archive store service
     * @throws ResultArchiveStoreException
     */
    public void addResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService)
            throws ResultArchiveStoreException {
        if (this.rasService != null) {
            throw new ResultArchiveStoreException("For the purposes of the MVP, only 1 RASS will be allowed");
        }
        this.rasService = resultArchiveStoreService;
    }

    public void setConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService) 
            throws ConfidentialTextException {
        if (this.ctsService != null) {
            throw new ConfidentialTextException("Invalid 2nd registration of the Confidential Text Service detected");
        }
        this.ctsService = confidentialTextService;
    }

    public void setCredentialsStoreService(@NotNull ICredentialsStoreService credentialsStoreService) 
            throws CredentialsStoreException {
        if (this.credsService != null) {
            throw new CredentialsStoreException("Invalid 2nd registration of the Credentials Store Service detected");
        }
        this.credsService = credentialsStoreService;
    }

    /**
     * Retrieve the active CPS Service
     *
     * @return The CPS Service
     */
    protected IConfigurationPropertyStoreService getConfigurationPropertyStoreService() {
        return this.cpsService;
    }

    /**
     * Retrieve the active DSS Service
     *
     * @return The DSS service
     */
    protected IDynamicStatusStoreService getDynamicStatusStoreService() {
        return this.dssService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IFramework#getTestRunId()
     */
    @Override
    public String getTestRunId() {
        try {
            return cpsFramework.getProperty("run", "id");
        } catch (ConfigurationPropertyStoreException e) {
           throw new UnsupportedOperationException("Appears to be running outside of a test run", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IFramework#getTestRunName()
     */
    @Override
    public String getTestRunName() {
        try {
            return cpsFramework.getProperty("run", "name");
        } catch (ConfigurationPropertyStoreException e) {
           throw new UnsupportedOperationException("Appears to be running outside of a test run", e);
        }
    }

}
