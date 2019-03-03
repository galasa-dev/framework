package io.ejat.framework;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import io.ejat.IConfidentialTextService;
import io.ejat.framework.internal.cps.FrameworkConfigurationPropertyStore;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
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

        validateNamespace(namespace);

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
        throw new UnsupportedOperationException("DSS has not been implemented yet");
    }

    /**
     * Validate the namespace adheres to naming standards
     *
     * @param namespace - the namespace to check
     * @throws ConfigurationPropertyStoreException - if the namespace does meet the
     *                                             standards
     */
    private void validateNamespace(String namespace) throws ConfigurationPropertyStoreException {
        if (namespace == null) {
            throw new ConfigurationPropertyStoreException("Namespace has not been provided");
        }

        final Matcher matcher = namespacePattern.matcher(namespace);
        if (!matcher.matches()) {
            throw new ConfigurationPropertyStoreException("Invalid namespace");
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
        throw new UnsupportedOperationException("CTS has not been implemented yet");
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
