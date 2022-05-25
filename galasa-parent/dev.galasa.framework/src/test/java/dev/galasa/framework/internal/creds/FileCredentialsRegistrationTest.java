/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.internal.creds;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.validation.constraints.NotNull;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

import dev.galasa.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.galasa.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.galasa.framework.internal.creds.FileCredentialsRegistration;
import dev.galasa.framework.spi.Api;
import dev.galasa.framework.spi.CertificateStoreException;
import dev.galasa.framework.spi.ConfidentialTextException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.creds.ICredentialsStore;

/**
 * <p>
 * This test class checks the behaviour of registering a local Credentials
 * Store.
 * </p>
 * 
 * @author Bruce Abbott
 */
public class FileCredentialsRegistrationTest {

    private Properties bootstrap;

    /**
     * <p>
     * This test method checks that a local Credentials Store can be registered and
     * initialised.
     * </p>
     * 
     * @throws IOException
     * @throws CredentialsStoreException
     * @throws URISyntaxException
     * @throws InvalidSyntaxException
     * @throws FrameworkException
     */
    @Test
    public void testInitialise()
            throws IOException, CredentialsException, URISyntaxException, InvalidSyntaxException, FrameworkException {
        File testCreds = File.createTempFile("galasa", ".properties");
        FileCredentialsRegistration fileCredsReg = new FileCredentialsRegistration();

        bootstrap = new Properties();
        bootstrap.setProperty("framework.config.store", "");

        FrameworkInitialisation fi = new FrameworkInitialisation(testCreds.toURI());
        fileCredsReg.initialise(fi);
        assertTrue("Dummy", true);
    }

    /**
     * <p>
     * This class is used to test the implemented methods in the tests above. They
     * are all noddy methods.
     * </p>
     */
    private class FrameworkInitialisation implements IFrameworkInitialisation {
        private URI                           uri;
        private FpfConfigurationPropertyStore fpf;
        private IFramework                    framework;
        private Properties                    overrides;
        private Properties                    records;

        public FrameworkInitialisation(URI uri) {
            this.framework = new Framework();
            this.uri = uri;
            try {
                fpf = new FpfConfigurationPropertyStore(uri);
            } catch (ConfigurationPropertyStoreException e) {

            }
        }

        @Override
        public void registerConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService)
                throws ConfidentialTextException {
        }

        @Override
        public URI getBootstrapConfigurationPropertyStore() {
            return uri;
        }

        @Override
        public void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore)
                throws DynamicStatusStoreException {
        }

        @Override
        public IFramework getFramework() {
            return this.framework;
        }

        @Override
        public void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore)
                throws ConfigurationPropertyStoreException {
        }

        @Override
        public URI getDynamicStatusStoreUri() {
            return null;
        }

        @Override
        public URI getCredentialsStoreUri() {
            System.out.println(uri);
            return uri;
        }

        @Override
        public List<URI> getResultArchiveStoreUris() {
            return null;
        }

        @Override
        public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService) {
        }

        @Override
        public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsException {
        }

		@Override
		public void registerCertificateStoreService(@NotNull ICertificateStoreService certificateStoreService)
				throws CertificateStoreException {
		}
    }

    /**
     * <p>
     * This is a private class used to implement the IFramework for testing
     * purposes.
     * </p>
     */
    private class Framework implements IFramework {
        private Properties overrides = new Properties();
        private Properties records   = new Properties();

        public IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
                throws ConfigurationPropertyStoreException {
            FrameworkConfigurationPropertyService fcps;
            try {
                File testFile = File.createTempFile("test", ".properties");
                Framework framework = new Framework();

                fcps = new FrameworkConfigurationPropertyService(framework,
                        new FpfConfigurationPropertyStore(testFile.toURI()), overrides, records, "framework");
                return fcps;

            } catch (Exception e) {
                System.out.println("Exception");
            }

            return null;
        }

        public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
                throws DynamicStatusStoreException {
            return null;
        }

        public IResultArchiveStore getResultArchiveStore() {
            return null;
        }

        public IResourcePoolingService getResourcePoolingService() {
            return null;
        }

        @Override
        public @NotNull IConfidentialTextService getConfidentialTextService() {
            return null;
        }

        @Override
        public String getTestRunName() {
            return null;
        }

        @Override
        public ICredentialsService getCredentialsService() throws CredentialsException {
            return null;
        }

        @Override
        public Random getRandom() {
            return null;
        }

        @Override
        public IRun getTestRun() {
            return null;
        }

        @Override
        public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
            return null;
        }

        @Override
        public void setFrameworkProperties(Properties overrideProperties) {
        }

        @Override
        public boolean isInitialised() {
            return false;
        }

        @Override
        public Properties getRecordProperties() {
            return null;
        }

        @Override
        public URL getApiUrl(@NotNull Api api) throws FrameworkException {
            return null;
        }


        @Override
        public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
            return null;
        }

		@Override
		public @NotNull ICertificateStoreService getCertifacteStoreService() {
			return null;
		}    }
}
