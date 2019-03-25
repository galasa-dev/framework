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
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ResultArchiveStoreException;

public class FrameworkInitialisation implements IFrameworkInitialisation {
    
    private static final String SCHEME_FILE = "file://";
    private static final String USER_HOME   = "user.home";

    private final Framework                   framework;
    private final Properties                  bootstrapProperties;
    private final Properties                  overrideProperties;
    private final Properties                  recordProperties = new Properties();

    private final URI                         uriConfigurationPropertyStore;
    private final URI                         uriDynamicStatusStore;
    private final List<URI>                   uriResultArchiveStores;

    private final IConfigurationPropertyStore cpsFramework;

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
        this.logger.debug("Configuration Property Store is " + propUri);

        // *** Initialise the Configuration Property Store
        this.logger.trace("Searching for CPS providers");
        final BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        final ServiceReference<?>[] cpsServiceReference = bundleContext
                .getAllServiceReferences(IConfigurationPropertyStoreService.class.getName(), null);
        if ((cpsServiceReference == null) || (cpsServiceReference.length == 0)) {
            throw new FrameworkException("No Configuration Property Store Services have been found");
        }
        for (final ServiceReference<?> cpsReference : cpsServiceReference) {
            final IConfigurationPropertyStoreService cpsService = (IConfigurationPropertyStoreService) bundleContext
                    .getService(cpsReference);
            this.logger.trace("Found CPS Provider " + cpsService.getClass().getName());
            cpsService.initialise(this);
        }
        if (this.framework.getConfigurationPropertyStoreService() == null) {
            throw new FrameworkException("Failed to initialise a Configuration Property Store, unable to continue");
        }
        this.logger.debug("Selected CPS Service is "
                + this.framework.getConfigurationPropertyStoreService().getClass().getName());

        // *** Set up a CPS store for framework
        this.cpsFramework = this.framework.getConfigurationPropertyStore("framework");

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

        // *** Initialise the Dynamic Status Store
        this.logger.trace("Searching for DSS providers");
        final ServiceReference<?>[] dssServiceReference = bundleContext
                .getAllServiceReferences(IDynamicStatusStoreService.class.getName(), null);
        if ((dssServiceReference == null) || (dssServiceReference.length == 0)) {
            throw new FrameworkException("No Dynamic Status Store Services have been found");
        }
        for (final ServiceReference<?> dssReference : dssServiceReference) {
            final IDynamicStatusStoreService dssService = (IDynamicStatusStoreService) bundleContext
                    .getService(dssReference);
            this.logger.trace("Found DSS Provider " + dssService.getClass().getName());
            dssService.initialise(this);
        }
        if (this.framework.getDynamicStatusStoreService() == null) {
            throw new FrameworkException("Failed to initialise a Dynamic Status Store, unable to continue");
        }
        logger.debug("Selected DSS Service is " + this.framework.getDynamicStatusStoreService().getClass().getName());

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
                .debug("Selected RAS Service is " + this.framework.getResultArchiveStoreService().getClass().getName());

        this.logger.error("Framework implementation is incomplete");
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
    public void registerConfigurationPropertyStoreService(
            @NotNull IConfigurationPropertyStoreService configurationPropertyStoreService)
            throws ConfigurationPropertyStoreException {
        this.framework.setConfigurationPropertyStoreService(configurationPropertyStoreService);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IFrameworkInitialisation#
     * registerDynamicStatusStoreService(io.ejat.framework.spi.
     * IDynamicStatusStoreService)
     */
    @Override
    public void registerDynamicStatusStoreService(@NotNull IDynamicStatusStoreService dynamicStatusStoreService)
            throws DynamicStatusStoreException {
        this.framework.setDynamicStatusStoreService(dynamicStatusStoreService);
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
