package dev.voras.framework;

import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;

import dev.voras.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.voras.framework.internal.creds.FrameworkCredentialsService;
import dev.voras.framework.internal.dss.FrameworkDynamicStatusStoreService;
import dev.voras.framework.spi.ConfidentialTextException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.DynamicStatusStoreException;
import dev.voras.framework.spi.FrameworkException;
import dev.voras.framework.spi.FrameworkResourcePoolingService;
import dev.voras.framework.spi.IConfidentialTextService;
import dev.voras.framework.spi.IConfigurationPropertyStore;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IDynamicStatusStore;
import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IFrameworkRuns;
import dev.voras.framework.spi.IResourcePoolingService;
import dev.voras.framework.spi.IResultArchiveStore;
import dev.voras.framework.spi.IResultArchiveStoreService;
import dev.voras.framework.spi.IRun;
import dev.voras.framework.spi.ResultArchiveStoreException;
import dev.voras.framework.spi.creds.CredentialsException;
import dev.voras.framework.spi.creds.ICredentialsService;
import dev.voras.framework.spi.creds.ICredentialsStore;

@Component(scope=ServiceScope.SINGLETON)
public class Framework implements IFramework {
	
	private final static Log                   logger = LogFactory.getLog(Framework.class);

    private static final Pattern               namespacePattern = Pattern.compile("[a-z0-9]+");

    private Properties                         overrideProperties;
    private final Properties                   recordProperties = new Properties();

    private IConfigurationPropertyStore        cpsStore;
    private IDynamicStatusStore                dssStore;
    private IResultArchiveStoreService         rasService;
    private IConfidentialTextService           ctsService;
    private ICredentialsStore                  credsStore;             

    @SuppressWarnings("unused")
	private IConfigurationPropertyStoreService cpsFramework;
    @SuppressWarnings("unused")
	private ICredentialsService                credsFramework;
    
    private String                             runName;
    
    private final Random                       random;
    
    private FrameworkRuns                      frameworkRuns;
    
    private TestRunLogCapture                  testRunLogCapture;
    
    private boolean                            initialised;

	private IRun run;
    
    public Framework() {
        this.random             = new Random();
    }
    
    @Activate
    public void activate() {    	
    	logger.info("Framework service activated");
    	logger.info("Framework version = " + FrameworkVersion.getBundleVersion());
    	logger.info("Framework build   = " + FrameworkVersion.getBundleBuild());
    }
    
    @Deactivate
    public void deactivate() {
    	if (this.testRunLogCapture != null) {
    		this.testRunLogCapture.shutdown();
    	}
    	logger.info("Framework service deactivated");
    }
    
    
	public void setFrameworkProperties(Properties overridesProperties) {
		this.overrideProperties = overridesProperties;
	}
	
    @Override
	public boolean isInitialised() {
		return this.initialised;
	}
    
    @Override
	public void initialisationComplete() {
		this.initialised = true;
	}

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
        if (this.cpsStore == null) {
            throw new ConfigurationPropertyStoreException("The Configuration Property Store has not been initialised");
        }

        try {
            validateNamespace(namespace);
        } catch (FrameworkException e) {
            throw new ConfigurationPropertyStoreException("Unable to provide Configuration Property Store", e);
        }

        return new FrameworkConfigurationPropertyService(this, this.cpsStore, this.overrideProperties,
                this.recordProperties, namespace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IFramework#getDynamicStatusStore(java.lang.String)
     */
    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
            throws DynamicStatusStoreException {
        if (this.dssStore == null) {
            throw new DynamicStatusStoreException("The Dynamic Status Store has not been initialised");
        }

        try {
            validateNamespace(namespace);
        } catch (FrameworkException e) {
            throw new DynamicStatusStoreException("Unable to provide Dynamic Status Store", e);
        }

        return new FrameworkDynamicStatusStoreService(this, this.dssStore, namespace);
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
        return new FrameworkResourcePoolingService();
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
    public @NotNull ICredentialsService getCredentialsService() throws CredentialsException {
        if (this.credsStore == null) {
            throw new CredentialsException("The Credentials Store has not been initialised");
        }

        return new FrameworkCredentialsService(this, this.credsStore);
    }

    /**
     * Set the new Configuration Property Store Service
     *
     * @param cpsService - The new CPS
     * @throws ConfigurationPropertyStoreException - If a CPS has already be
     *                                             registered
     */
    protected void setConfigurationPropertyStore(@NotNull IConfigurationPropertyStore cpsStore)
            throws ConfigurationPropertyStoreException {
        if (this.cpsStore != null) {
            throw new ConfigurationPropertyStoreException(
                    "Invalid 2nd registration of the Config Property Store Service detected");
        }

        this.cpsStore = cpsStore;
        this.cpsFramework = getConfigurationPropertyService("framework");
    }

    public void setDynamicStatusStore(@NotNull IDynamicStatusStore dssStore)
            throws DynamicStatusStoreException {
        if (this.dssStore != null) {
            throw new DynamicStatusStoreException(
                    "Invalid 2nd registration of the Dynamic Status Store Service detected");
        }

        this.dssStore = dssStore;
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

    public void setCredentialsStore(@NotNull ICredentialsStore credsStore) 
            throws CredentialsException {
        if (this.credsStore != null) {
            throw new CredentialsException("Invalid 2nd registration of the Credentials Store Service detected");
        }
        this.credsStore = credsStore;
    }

    /**
     * Retrieve the active CPS Service
     *
     * @return The CPS Service
     */
    protected IConfigurationPropertyStore getConfigurationPropertyStore() {
        return this.cpsStore;
    }

    /**
     * Retrieve the active DSS Service
     *
     * @return The DSS service
     */
    protected IDynamicStatusStore getDynamicStatusStore() {
        return this.dssStore;
    }

    protected ICredentialsStore getCredentialsStore() {
        return this.credsStore;
    }

    @Override
    public Random getRandom() {
    	return this.random;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IFramework#getTestRunName()
     */
    @Override
    public String getTestRunName() {
    	return this.runName;
    }
    
	/**
	 * Set the run name if it is a test run
	 * 
	 * @param runName The run name
	 * @throws DynamicStatusStoreException 
	 */
	public void setTestRunName(String runName) throws FrameworkException {
		this.runName = runName;
		
		this.run = getFrameworkRuns().getRun(runName);
	}
	
	@Override
	public IRun getTestRun() {
		return this.run;
	}

	@Override
	public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
		if (this.frameworkRuns == null) {
			this.frameworkRuns = new FrameworkRuns(this);
		}
		
		return this.frameworkRuns;
	}
	
	@Override
	public Properties getRecordProperties() {
		Properties clone = (Properties) this.recordProperties.clone();
		return clone;
	}

	public void installLogCapture() {
		if (this.testRunLogCapture != null) {
			return;
		}
		
		this.testRunLogCapture = new TestRunLogCapture(this);
		
	}

}
