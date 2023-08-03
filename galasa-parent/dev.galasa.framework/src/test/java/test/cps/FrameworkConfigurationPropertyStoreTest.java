/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.cps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Random;

import javax.validation.constraints.NotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.galasa.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.galasa.framework.spi.Api;
import dev.galasa.framework.spi.CertificateStoreException;
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
import dev.galasa.framework.spi.creds.ICredentialsService;

/**
 * <p>
 * This test class tests the behaviour of the
 * FrameworkConfigurationPropertyStore class. The purpose of the class is to
 * drive the registered CPS service.
 * </p>
 */
public class FrameworkConfigurationPropertyStoreTest {
    File testProp;

    @Before
    public void makeSureTempPropertiesDeleted() throws IOException {
        testProp = File.createTempFile("galasafpf_", ".properties");
    }

    @After
    public void deletePropertiesFile() throws IOException {
        if (testProp != null && testProp.exists()) {
            testProp.delete();
        }
    }

    /**
     * <p>
     * This test method ensures the object can be insantiated
     * </p>
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkConstructor() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("Test1", "SomeString");
        testProps.setProperty("Test2", "SomeString");
        testProps.setProperty("RootPasswordForEverySystemEver", "admin");
        testProps.setProperty("anotherString", "anotherString");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new Framework(), fpfcps,
                overrides, record, "zos");
        assertNotNull("Framework CPS could not bre created", test);
    }

    /**
     * <p>
     * This test method does a simple get property check from a basic properties
     * file.
     * </p>
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkGetProperty() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();
        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new Framework(), fpfcps,
                overrides, record, "zos");
        assertEquals("Unexpected Value retrieved from CPS", "Waddup",
                test.getProperty("image", "credentialid", "PLEXMA"));
    }

    /**
     * <p>
     * This test method ensures that the getProperty method retrieves the preferred
     * key when two viable options available.
     * </p>
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkGetPropertyWithMultipleViableOptions()
            throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new Framework(), fpfcps,
                overrides, record, "zos");
        assertEquals("Unexpected Value retrieved from CPS", "Spoon",
                test.getProperty("image", "credentialid", "PLEXMA", "MVMA"));
    }

    /**
     * <p>
     * This test method ensures that the getProperty method checks the overrides and
     * retrieves the correct values over the CPS stored version.
     * </p>
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkGetPropertyFromOverrides() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");

        overrides.setProperty("zos.image.PLEXMA.credentialid", "Sever2");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new Framework(), fpfcps,
                overrides, record, "zos");
        assertEquals("Unexpected Value retrieved from CPS", "Sever2",
                test.getProperty("image", "credentialid", "PLEXMA"));
    }

    @Test
    public void testNullInfixes() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");
        testProps.setProperty("zos.image.credentialid", "tab!=space");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new Framework(), fpfcps,
                overrides, record, "zos");
        assertEquals("Unexpected Value retrieved from CPS", "tab!=space", test.getProperty("image", "credentialid"));
    }

    @Test
    public void testReportOfSearchKeys() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");
        testProps.setProperty("zos.image.credentialid", "tab!=space");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new Framework(), fpfcps,
                overrides, record, "zos");
        assertEquals("Unexpected Key retrieved from CPS", "zos.image.PLEXMA.MVMA.credentialid",
                test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[0]);
        assertEquals("Unexpected Key retrieved from CPS", "zos.image.PLEXMA.credentialid",
                test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[1]);
        assertEquals("Unexpected Key retrieved from CPS", "zos.image.credentialid",
                test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[2]);
    }

    /**
     * <p>
     * This is a private class used to implement the IFramework for testing
     * purposes.
     * </p>
     */
    private class Framework implements IFramework {
        public IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
                throws ConfigurationPropertyStoreException {
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
        public ICredentialsService getCredentialsService() {
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
		public @NotNull ICertificateStoreService getCertificateStoreService() {
			return null;
		}
    }

    /**
     * <p>
     * This is a private class used to implement the IFrameworkIntialisation for
     * testing purposes.
     * </p>
     */
    // private class FrameworkInitialisation implements IFrameworkInitialisation {
    // private URI uri;

    // public FrameworkInitialisation(URI uri) {
    // this.uri=uri;
    // }
    // @Override
    // public void registerConfidentialTextService(@NotNull IConfidentialTextService
    // cts) throws ConfidentialTextException{
    // }

    // @Override
    // public URI getBootstrapConfigurationPropertyStore() {return uri;}
    // @Override
    // public void registerDynamicStatusStoreService(IDynamicStatusStoreService
    // dynamicStatusStoreService){}
    // @Override
    // public IFramework getFramework(){return null;}

    // @Override
    // public void registerConfigurationPropertyStore(@NotNull
    // IConfigurationPropertyStore configurationPropertyStore)
    // throws ConfigurationPropertyStoreException {}

    // @Override
    // public URI getDynamicStatusStoreUri() {return null;}

    // @Override
    // public List<URI> getResultArchiveStoreUris() {return null;}

    // @Override
    // public void registerResultArchiveStoreService(@NotNull
    // IResultArchiveStoreService resultArchiveStoreService) {
    // }
    // }
}
