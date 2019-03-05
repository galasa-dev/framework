package test.cps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import io.ejat.IConfidentialTextService;
import io.ejat.framework.internal.cps.FpfConfigurationPropertyStoreService;
import io.ejat.framework.internal.cps.FrameworkConfigurationPropertyStore;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStore;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResourcePoolingService;
import io.ejat.framework.spi.IResultArchiveStore;
import io.ejat.framework.spi.IResultArchiveStoreService;

/**
 * <p>This test class tests the behaviour of the FrameworkConfigurationPropertyStore class. The purpose of the class is to drive
 * the registered CPS service. </p>
 */
public class FrameworkConfigurationPropertyStoreTest {
    File testProp;

    @Before
    public void makeSureTempPropertiesDeleted() throws IOException{
            testProp = File.createTempFile("ejatfpf_", ".properties");
    }

    @After
    public void deletePropertiesFile() throws IOException{
    	if (testProp != null && testProp.exists()) {
    		testProp.delete();
    	}
    }

    /**
     * <p> This test method ensures the object can be insantiated</p>
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

        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(testProp.toURI()));

        FrameworkConfigurationPropertyStore test = new FrameworkConfigurationPropertyStore(new Framework(), fpfcps, overrides, record, "zos");
        assertNotNull("Framework CPS could not bre created",test);
    } 

    /**
     * <p>This test method does a simple get property check from a basic properties file.</p>
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

        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(testProp.toURI()));

        FrameworkConfigurationPropertyStore test = new FrameworkConfigurationPropertyStore(new Framework(), fpfcps, overrides, record, "zos");
        assertEquals("Unexpected Value retrieved from CPS", "Waddup", test.getProperty("image", "credentialid", "PLEXMA"));
    } 

    /**
     * <p>This test method ensures that the getProperty method retrieves the preferred key when two viable options available.</p>
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkGetPropertyWithMultipleViableOptions() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();
        
        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(testProp.toURI()));

        FrameworkConfigurationPropertyStore test = new FrameworkConfigurationPropertyStore(new Framework(), fpfcps, overrides, record, "zos");
        assertEquals("Unexpected Value retrieved from CPS", "Spoon", test.getProperty("image", "credentialid", "PLEXMA", "MVMA"));
    }

    /**
     * <p>This test method ensures that the getProperty method checks the overrides and retrieves the correct values over the CPS stored version.</p>
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

        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(testProp.toURI()));

        FrameworkConfigurationPropertyStore test = new FrameworkConfigurationPropertyStore(new Framework(), fpfcps, overrides, record, "zos");
        assertEquals("Unexpected Value retrieved from CPS", "Sever2", test.getProperty("image", "credentialid", "PLEXMA"));
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

        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(testProp.toURI()));

        FrameworkConfigurationPropertyStore test = new FrameworkConfigurationPropertyStore(new Framework(), fpfcps, overrides, record, "zos");
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

        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(testProp.toURI()));

        FrameworkConfigurationPropertyStore test = new FrameworkConfigurationPropertyStore(new Framework(), fpfcps, overrides, record, "zos");
        assertEquals("Unexpected Key retrieved from CPS", "zos.image.PLEXMA.MVMA.credentialid", test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[0]);
        assertEquals("Unexpected Key retrieved from CPS", "zos.image.PLEXMA.credentialid", test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[1]);
        assertEquals("Unexpected Key retrieved from CPS", "zos.image.credentialid", test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[2]);
    }

    /**
     * <p>This is a private class used to implement the IFramework for testing purposes.</p>
     */
    private class Framework implements IFramework{
        public IConfigurationPropertyStore getConfigurationPropertyStore(@NotNull String namespace) throws ConfigurationPropertyStoreException {
            return null;
        }

        public IDynamicStatusStore getDynamicStatusStore(@NotNull String namespace) throws DynamicStatusStoreException {
            return null;
        }
        public IResultArchiveStore getResultArchiveStore(){return null;}
        public IResourcePoolingService getResourcePoolingService(){return null;}

		@Override
		public @NotNull IConfidentialTextService getConfidentialTextService() {return null;}

		@Override
		public String getTestRunId() {
			return null;
		}

		@Override
		public String getTestRunName() {
			return null;
		}
        
    } 

    /**
     * <p>This is a private class used to implement the IFrameworkIntialisation for testing purposes.</p>
     */
    private class FrameworkInitialisation implements IFrameworkInitialisation {
        private URI uri;

        public FrameworkInitialisation(URI uri) {
            this.uri=uri;
        }

        @Override
        public URI getBootstrapConfigurationPropertyStore() {return uri;}
        @Override
        public void registerDynamicStatusStoreService(IDynamicStatusStoreService dynamicStatusStoreService){}
        @Override
        public IFramework getFramework(){return null;}
        
        @Override
        public void registerConfigurationPropertyStoreService(@NotNull IConfigurationPropertyStoreService configurationPropertyStoreService)
                throws ConfigurationPropertyStoreException {}

		@Override
		public URI getDynamicStatusStoreUri() {return null;}

		@Override
		public List<URI> getResultArchiveStoreUris() {return null;}

		@Override
		public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService) {
		}
    }
}