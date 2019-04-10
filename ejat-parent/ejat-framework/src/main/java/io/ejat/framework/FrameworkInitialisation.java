package io.ejat.framework;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import io.ejat.framework.spi.ConfidentialTextException;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.FrameworkException;
import io.ejat.framework.spi.IConfidentialTextService;
import io.ejat.framework.spi.IConfigurationPropertyStore;
import io.ejat.framework.spi.IConfigurationPropertyStoreRegistration;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.IDynamicStatusStoreRegistration;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ResultArchiveStoreException;
import io.ejat.framework.spi.creds.CredentialsException;
import io.ejat.framework.spi.creds.ICredentialsStore;
import io.ejat.framework.spi.creds.ICredentialsStoreRegistration;

public class FrameworkInitialisation implements IFrameworkInitialisation {

    private static final String               SCHEME_FILE      = "file://";
    private static final String               USER_HOME        = "user.home";

    private final Framework                   framework;
    private final Properties                  bootstrapProperties;
    private final Properties                  overrideProperties;
    private final Properties                  recordProperties = new Properties();

    private final URI                         uriConfigurationPropertyStore;
    private final URI                         uriDynamicStatusStore;
    private final URI                         uriCredentialsStore;
    private final List<URI>                   uriResultArchiveStores;

    private final IConfigurationPropertyStoreService cpsFramework;
    //private final ICredentialsStoreService credsFramework;

    private final Log                         logger           = LogFactory.getLog(this.getClass());

    public FrameworkInitialisation(Properties bootstrapProperties, Properties overrideProperties)
            throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this.bootstrapProperties = bootstrapProperties;
        this.overrideProperties = overrideProperties;

        this.logger.info("Initialising the eJAT Framework");

        this.framework = new Framework(this.overrideProperties, this.recordProperties);

        final String propUri = this.bootstrapProperties.getProperty("framework.config.store");
        if ((propUri == null) || propUri.isEmpty()) {
            this.uriConfigurationPropertyStore = new URI(
                    SCHEME_FILE + System.getProperty(USER_HOME) + "/.ejat/cps.properties");
        } else {
            this.uriConfigurationPropertyStore = new URI(propUri);
        }
        this.logger.debug("Configuration Property Store is " + this.uriConfigurationPropertyStore.toString());

        // *** Initialise the Configuration Property Store
        this.logger.trace("Searching for CPS providers");
        final BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
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
        this.logger.debug("Selected CPS Service is "
                + this.framework.getConfigurationPropertyStore().getClass().getName());

        // *** Set up a CPS store for framework
        this.cpsFramework = this.framework.getConfigurationPropertyService("framework");

        // *** Work out the dss uri
        try {
            final String dssProperty = this.cpsFramework.getProperty("dynamicstatus", "store");
            if ((dssProperty == null) || dssProperty.isEmpty()) {
                this.uriDynamicStatusStore = new URI(
                        SCHEME_FILE + System.getProperty(USER_HOME) + "/.ejat/dss.properties");
            } else {
                this.uriDynamicStatusStore = new URI(dssProperty);
            }
        } catch (final Exception e) {
            throw new FrameworkException("Unable to resolve the Dynamic Status Store URI", e);
        }
        this.logger.debug("Dynamic Status Store is " + this.uriDynamicStatusStore.toString());

        // *** Work out the ras uris
        try {
            final String rasProperty = this.cpsFramework.getProperty("resultarchive", "store");
            this.uriResultArchiveStores = new ArrayList<>(1);
            if ((rasProperty == null) || rasProperty.isEmpty()) {
                this.uriResultArchiveStores.add(new URI(SCHEME_FILE + System.getProperty(USER_HOME) + "/.ejat/ras"));
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
        this.logger.debug("Result Archive Stores are " + this.uriResultArchiveStores.toString());

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

        // *** Initialise the Result Archive Store
        this.logger.trace("Searching for RAS providers");
        final ServiceReference<?>[] rasServiceReference = bundleContext
                .getAllServiceReferences(IResultArchiveStoreService.class.getName(), null);
        if ((rasServiceReference == null) || (rasServiceReference.length == 0)) {
            throw new FrameworkException("No Result Archive Store Services have been found");
        }
        for (final ServiceReference<?> rasReference : rasServiceReference) {
            final IResultArchiveStoreService rasService = (IResultArchiveStoreService) bundleContext
                    .getService(rasReference);
            this.logger.trace("Found RAS Provider " + rasService.getClass().getName());
            rasService.initialise(this);
        }
        if (this.framework.getResultArchiveStoreService() == null) {
            throw new FrameworkException("Failed to initialise a Result Archive Store, unable to continue");
        }
        this.logger
                .trace("Selected RAS Service is " + this.framework.getResultArchiveStoreService().getClass().getName());

        // *** Work out the creds uri
        try {
            final String credsProperty = this.cpsFramework.getProperty("credentials", "store");
            if ((credsProperty == null) || credsProperty.isEmpty()) {
                this.uriCredentialsStore = new URI(
                        SCHEME_FILE + System.getProperty(USER_HOME) + "/.ejat/credentials.properties");
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
            throw new FrameworkException("Failed to initialise a Credentuals Store, unable to continue");
        }
        this.logger
                .trace("Selected Credentials Service is " + this.framework.getCredentialsStore().getClass().getName());
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IFrameworkInitialisation#
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
     * io.ejat.framework.spi.IFrameworkInitialisation#getDynamicStatusStoreUri()
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
     * io.ejat.framework.spi.IFrameworkInitialisation#getResultArchiveStoreUris()
     */
    @Override
    public List<URI> getResultArchiveStoreUris() {
        return this.uriResultArchiveStores;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IFrameworkInitialisation#
     * registerConfigurationPropertyStoreService(io.ejat.framework.spi.
     * IConfigurationPropertyStoreService)
     */
    @Override
    public void registerConfigurationPropertyStore(
            @NotNull IConfigurationPropertyStore configurationPropertyStore)
            throws ConfigurationPropertyStoreException {
        this.framework.setConfigurationPropertyStore(configurationPropertyStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IFrameworkInitialisation#
     * registerDynamicStatusStoreService(io.ejat.framework.spi.
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
     * @see io.ejat.framework.spi.IFrameworkInitialisation#
     * registerResultArchiveStoreService(io.ejat.framework.spi.
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
    public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore)
            throws CredentialsException {
        this.framework.setCredentialsStore(credentialsStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IFrameworkInitialisation#getFramework()
     */
    @Override
    public @NotNull IFramework getFramework() {
        return this.framework;
    }

}
