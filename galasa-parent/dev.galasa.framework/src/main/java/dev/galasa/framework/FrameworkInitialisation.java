/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.CertificateStoreException;
import dev.galasa.framework.spi.ConfidentialTextException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfidentialTextServiceRegistration;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IConfigurationPropertyStoreRegistration;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreRegistration;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResultArchiveStoreRegistration;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;
import dev.galasa.framework.spi.creds.ICredentialsStoreRegistration;

public class FrameworkInitialisation implements IFrameworkInitialisation {

    private static final String                      USER_HOME = "user.home";

    private Framework                                framework;

    private final Properties                         bootstrapProperties;

    private final URI                                uriConfigurationPropertyStore;
    private final URI                                uriDynamicStatusStore;
    private final URI                                uriCredentialsStore;
    private final List<URI>                          uriResultArchiveStores;

    private final IConfigurationPropertyStoreService cpsFramework;
    private final IDynamicStatusStoreService         dssFramework;
    // private final ICredentialsStoreService credsFramework;

    private Log logger;

    public FrameworkInitialisation(Properties bootstrapProperties, Properties overrideProperties)
            throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this(bootstrapProperties, overrideProperties, false, null);
    }

    public FrameworkInitialisation(Properties bootstrapProperties, Properties overrideProperties, boolean testrun)
            throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this(bootstrapProperties, overrideProperties, testrun, null);
    }

    public FrameworkInitialisation(Properties bootstrapProperties, Properties overrideProperties, boolean testrun,
            Log initLogger) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this.bootstrapProperties = bootstrapProperties;

        // *** Copy the the bootstrap properties to the override properties so that they
        // are available to the managers
        overrideProperties.putAll(bootstrapProperties);

        if (initLogger == null) {
            logger = LogFactory.getLog(this.getClass());
        } else {
            logger = initLogger;
        }

        this.logger.info("Initialising the Galasa Framework");

        // *** Locate the framework
        final BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference<IFramework> frameworkService = bundleContext.getServiceReference(IFramework.class);
        if (frameworkService == null) {
            throw new FrameworkException("The framework service is missing");
        }
        this.framework = (Framework) bundleContext.getService(frameworkService);
        if (this.framework.isInitialised()) {
            throw new FrameworkException("The framework has already been initialised");
        }
        this.framework.setFrameworkProperties(overrideProperties);

        // *** If this is a test run, then we need to install the log4j capture routine
        if (testrun) {
            framework.installLogCapture();
        }

        String propUri = System.getenv("GALASA_CONFIG_STORE");
        if ((propUri == null) || propUri.isEmpty()) {
            propUri = this.bootstrapProperties.getProperty("framework.config.store");
        }
        if ((propUri == null) || propUri.isEmpty()) {
            this.uriConfigurationPropertyStore = Paths.get(System.getProperty(USER_HOME), ".galasa", "cps.properties")
                    .toUri();
            createIfMissing(this.uriConfigurationPropertyStore);
        } else {
            this.uriConfigurationPropertyStore = new URI(propUri);
        }
        this.logger.debug("Configuration Property Store is " + this.uriConfigurationPropertyStore.toString());

        // *** Initialise the Configuration Property Store
        this.logger.trace("Searching for CPS providers");
        final ServiceReference<?>[] cpsServiceReference = bundleContext
                .getAllServiceReferences(IConfigurationPropertyStoreRegistration.class.getName(), null);
        if ((cpsServiceReference == null) || (cpsServiceReference.length == 0)) {
            throw new FrameworkException("No Configuration Property Store Services have been found");
        }
        for (final ServiceReference<?> cpsReference : cpsServiceReference) {
            final IConfigurationPropertyStoreRegistration cpsStoreRegistration = (IConfigurationPropertyStoreRegistration) bundleContext
                    .getService(cpsReference);
            this.logger.trace("Found CPS Provider " + cpsStoreRegistration.getClass().getName());
            cpsStoreRegistration.initialise(this);
        }
        if (this.framework.getConfigurationPropertyStore() == null) {
            throw new FrameworkException("Failed to initialise a Configuration Property Store, unable to continue");
        }
        this.logger.debug(
                "Selected CPS Service is " + this.framework.getConfigurationPropertyStore().getClass().getName());

        // *** Set up a CPS store for framework
        this.cpsFramework = this.framework.getConfigurationPropertyService("framework");

        // *** Work out the dss uri
        try {
            String dssProperty = System.getenv("GALASA_DYNAMICSTATUS_STORE");
            if ((dssProperty == null) || dssProperty.isEmpty()) {
                dssProperty = this.cpsFramework.getProperty("dynamicstatus", "store");
            }
            if ((dssProperty == null) || dssProperty.isEmpty()) {
                this.uriDynamicStatusStore = Paths.get(System.getProperty(USER_HOME), ".galasa", "dss.properties")
                        .toUri();
                createIfMissing(this.uriDynamicStatusStore);
            } else {
                this.uriDynamicStatusStore = new URI(dssProperty);
            }
        } catch (final Exception e) {
            throw new FrameworkException("Unable to resolve the Dynamic Status Store URI", e);
        }
        this.logger.debug("Dynamic Status Store is " + this.uriDynamicStatusStore.toString());

        // *** Initialise the Dynamic Status Store
        this.logger.trace("Searching for DSS providers");
        final ServiceReference<?>[] dssServiceReference = bundleContext
                .getAllServiceReferences(IDynamicStatusStoreRegistration.class.getName(), null);
        if ((dssServiceReference == null) || (dssServiceReference.length == 0)) {
            throw new FrameworkException("No Dynamic Status Store Services have been found");
        }
        for (final ServiceReference<?> dssReference : dssServiceReference) {
            final IDynamicStatusStoreRegistration dssStoreRegistration = (IDynamicStatusStoreRegistration) bundleContext
                    .getService(dssReference);
            this.logger.trace("Found DSS Provider " + dssStoreRegistration.getClass().getName());
            dssStoreRegistration.initialise(this);
        }
        if (this.framework.getDynamicStatusStore() == null) {
            throw new FrameworkException("Failed to initialise a Dynamic Status Store, unable to continue");
        }
        logger.trace("Selected DSS Service is " + this.framework.getDynamicStatusStore().getClass().getName());
        // *** Set up the DSS for the framework
        this.dssFramework = this.framework.getDynamicStatusStoreService("framework");

        // *** Is this a test run,
        // *** Then we need to make sure we have a runname for the RAS. If there isnt
        // one, we need to allocate one
        // *** Need the DSS for this as the latest run number number is stored in there
        if (testrun) {
            //*** Ensure the shared environment = true is set for Shenv runs
            String runName = AbstractManager.nulled(this.cpsFramework.getProperty("run", "name"));
            if (runName == null) {
                String testName = AbstractManager.nulled(this.cpsFramework.getProperty("run", "testbundleclass"));
                String testLanguage  = "java";
                if (testName == null) {
                    testName = AbstractManager.nulled(this.cpsFramework.getProperty("run", "gherkintest"));
                    testLanguage = "gherkin";
                }
                runName = createRunName(testName, testLanguage);
                framework.setTestRunName(runName);
            } else {
                framework.setTestRunName(runName);
            }
        }

        // *** Work out the ras uris
        Path localRas = Paths.get(System.getProperty(USER_HOME), ".galasa", "ras");
        try {
            String rasProperty = System.getenv("GALASA_RESULTARCHIVE_STORE");
            if ((rasProperty == null) || rasProperty.isEmpty()) {
                rasProperty = this.cpsFramework.getProperty("resultarchive", "store");
            }
            this.uriResultArchiveStores = new ArrayList<>(1);
            if ((rasProperty == null) || rasProperty.isEmpty()) {
                this.uriResultArchiveStores.add(localRas.toUri());
            } else {
                final String[] rass = rasProperty.split(",");
                for (final String ras : rass) {
                    if (!ras.trim().isEmpty()) {
                        this.uriResultArchiveStores.add(new URI(ras));
                    }
                }
                if (this.uriResultArchiveStores.isEmpty()) {
                    throw new FrameworkException("No Result Archive Store URIs were provided");
                }
            }
        } catch (final FrameworkException e) {
            throw e;
        } catch (final Exception e) {
            throw new FrameworkException("Unable to resolve the Result Archive Store URIs", e);
        }

        Boolean includeLocal = Boolean
                .parseBoolean(this.cpsFramework.getProperty("resultarchive.store", "include.default.local"));
        if (includeLocal) {
            boolean alreadyThere = false;
            for (URI ras : this.uriResultArchiveStores) {
                if (ras.equals(localRas.toUri())) {
                    alreadyThere = true;
                    break;
                }
            }

            if (!alreadyThere) {
                this.uriResultArchiveStores.add(localRas.toUri());
            }
        }

        this.logger.debug("Result Archive Stores are " + this.uriResultArchiveStores.toString());

        // *** Initialise the Result Archive Store
        this.logger.trace("Searching for RAS providers");
        final ServiceReference<?>[] rasServiceReference = bundleContext
                .getAllServiceReferences(IResultArchiveStoreRegistration.class.getName(), null);
        if ((rasServiceReference == null) || (rasServiceReference.length == 0)) {
            throw new FrameworkException("No Result Archive Store Services have been found");
        }
        for (final ServiceReference<?> rasReference : rasServiceReference) {
            final IResultArchiveStoreRegistration rasRegistration = (IResultArchiveStoreRegistration) bundleContext
                    .getService(rasReference);
            this.logger.trace("Found RAS Provider " + rasRegistration.getClass().getName());
            rasRegistration.initialise(this);
        }
        if (this.framework.getResultArchiveStoreService() == null) {
            throw new FrameworkException("Failed to initialise a Result Archive Store, unable to continue");
        }
        this.logger
        .trace("Selected RAS Service is " + this.framework.getResultArchiveStoreService().getClass().getName());

        // *** Work out the creds uri
        try {
            String credsProperty = System.getenv("GALASA_CREDENTIALS_STORE");
            if ((credsProperty == null) || credsProperty.isEmpty()) {
                credsProperty = this.cpsFramework.getProperty("credentials", "store");
            }
            if ((credsProperty == null) || credsProperty.isEmpty()) {
                this.uriCredentialsStore = Paths.get(System.getProperty(USER_HOME), ".galasa", "credentials.properties")
                        .toUri();
                createIfMissing(this.uriCredentialsStore);
            } else {
                this.uriCredentialsStore = new URI(credsProperty);
            }
        } catch (final Exception e) {
            throw new FrameworkException("Unable to resolve the Credentials Store URI", e);
        }
        this.logger.debug("Credentials Store is " + this.uriCredentialsStore.toString());

        // *** Initialise the Credentials Store
        this.logger.trace("Searching for Creds providers");
        final ServiceReference<?>[] credsServiceReference = bundleContext
                .getAllServiceReferences(ICredentialsStoreRegistration.class.getName(), null);
        if ((credsServiceReference == null) || (credsServiceReference.length == 0)) {
            throw new FrameworkException("No Credentials Services have been found");
        }
        for (final ServiceReference<?> credsReference : credsServiceReference) {
            final ICredentialsStoreRegistration credsRegistration = (ICredentialsStoreRegistration) bundleContext
                    .getService(credsReference);
            this.logger.trace("Found Creds Provider " + credsRegistration.getClass().getName());
            credsRegistration.initialise(this);
        }
        if (this.framework.getCredentialsStore() == null) {
            throw new FrameworkException("Failed to initialise a Credentials Store, unable to continue");
        }
        this.logger
        .trace("Selected Credentials Service is " + this.framework.getCredentialsStore().getClass().getName());

        // *** Initialise the Confidential Test Service
        this.logger.trace("Searching for Confidential Text Service providers");
        final ServiceReference<?>[] confidentialServiceReference = bundleContext
                .getAllServiceReferences(IConfidentialTextServiceRegistration.class.getName(), null);
        if ((confidentialServiceReference == null) || (confidentialServiceReference.length == 0)) {
            throw new FrameworkException("No Confidential Text Services have been found");
        }
        for (final ServiceReference<?> confidentialReference : confidentialServiceReference) {
            final IConfidentialTextServiceRegistration credsRegistration = (IConfidentialTextServiceRegistration) bundleContext
                    .getService(confidentialReference);
            this.logger.trace("Found Confidential Text Services Provider " + credsRegistration.getClass().getName());
            credsRegistration.initialise(this);
        }
        if (this.framework.getConfidentialTextService() == null) {
            throw new FrameworkException("Failed to initialise a Confidential Text Services, unable to continue");
        }
        this.logger.trace("Selected Confidential Text Service is "
                + this.framework.getConfidentialTextService().getClass().getName());

        if (framework.isInitialised()) {
            this.logger.info("Framework initialised");
        } else {
            this.logger.info("The Framework does not think it is initialised, but we didn't get any errors");
        }

        // *** If this is a test run, add the overrides from the run dss properties to
        // these overrides
        if (testrun) {
            String prefix = "run." + framework.getTestRunName() + ".override.";
            int len = prefix.length();

            Map<String, String> runOverrides = this.dssFramework.getPrefix(prefix);
            for (Entry<String, String> override : runOverrides.entrySet()) {
                String key = override.getKey().substring(len);
                String value = override.getValue();

                if (logger.isTraceEnabled()) {
                    logger.trace("Setting run override " + key + "=" + value);
                }
                overrideProperties.put(override.getKey(), override.getValue());
            }
        }
    }

    /**
     * Create a new run as this run was submitted from the command line, maybe
     * 
     * @param runBundleClass
     * @param language
     * @return The name of the run created.
     * @throws FrameworkException
     */
    protected String createRunName(String runBundleClass, String language) throws FrameworkException {
        IRun run = null;
        IFrameworkRuns frameworkRuns = this.framework.getFrameworkRuns();

        switch(language) {
            case "java": 
                String split[] = runBundleClass.split("/");
                String bundle = split[0];
                String test = split[1];
                run = frameworkRuns.submitRun("local", null, bundle, test, null, null, null, null, true, false, null, null, null, language);
                break;
            case "gherkin":
                run = frameworkRuns.submitRun("local", null, null, runBundleClass, null, null, null, null, true, false, null, null, null, language);
                break;
            default:
                throw new FrameworkException("Unknown language to create run");
        }

        logger.info("Allocated Run Name " + run.getName() + " to this run");

        return run.getName();
    }

    /**
     * Create an empty default property file if it doesn't already exist
     * 
     * @param propertyFile
     * @throws IOException
     */
    private void createIfMissing(URI propertyFile) {

        Path path = Paths.get(propertyFile);
        try {
            if (!path.toFile().exists()) {
                if (!path.getParent().toFile().exists()) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
            }
        } catch (IOException e) {
            logger.error("Unable to create empty default property file " + path.toUri().toString(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#
     * getBootstrapConfigurationPropertyStore()
     */
    @Override
    public @NotNull URI getBootstrapConfigurationPropertyStore() {
        return this.uriConfigurationPropertyStore;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.galasa.framework.spi.IFrameworkInitialisation#getDynamicStatusStoreUri()
     */
    @Override
    public URI getDynamicStatusStoreUri() {
        return this.uriDynamicStatusStore;
    }

    @Override
    public URI getCredentialsStoreUri() {
        return this.uriCredentialsStore;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.galasa.framework.spi.IFrameworkInitialisation#getResultArchiveStoreUris()
     */
    @Override
    public List<URI> getResultArchiveStoreUris() {
        return this.uriResultArchiveStores;
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#
     * registerConfigurationPropertyStoreService(dev.galasa.framework.spi.
     * IConfigurationPropertyStoreService)
     */
    @Override
    public void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore)
            throws ConfigurationPropertyStoreException {
        this.framework.setConfigurationPropertyStore(configurationPropertyStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#
     * registerDynamicStatusStoreService(dev.galasa.framework.spi.
     * IDynamicStatusStoreService)
     */
    @Override
    public void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore)
            throws DynamicStatusStoreException {
        this.framework.setDynamicStatusStore(dynamicStatusStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#
     * registerResultArchiveStoreService(dev.galasa.framework.spi.
     * IResultArchiveStoreService)
     */
    @Override
    public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService)
            throws ResultArchiveStoreException {
        this.framework.addResultArchiveStoreService(resultArchiveStoreService);

    }

    @Override
    public void registerConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService)
            throws ConfidentialTextException {
        this.framework.setConfidentialTextService(confidentialTextService);
    }

    @Override
    public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsException {
        this.framework.setCredentialsStore(credentialsStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#getFramework()
     */
    @Override
    public @NotNull IFramework getFramework() {
        return this.framework;
    }

    public void shutdownFramework() {
        try {
            this.framework.shutdown(null);
        } catch(Exception e) {
            logger.fatal("Problem shutting down the Galasa framework",e);
        }
    }

	@Override
	public void registerCertificateStoreService(@NotNull ICertificateStoreService certificateStoreService)
			throws CertificateStoreException {
		// TODO Auto-generated method stub
		
	}

}
