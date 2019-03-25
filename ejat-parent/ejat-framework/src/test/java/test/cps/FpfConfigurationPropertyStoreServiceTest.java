package test.cps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import io.ejat.framework.spi.IConfidentialTextService;
import io.ejat.framework.internal.cps.FpfConfigurationPropertyStoreService;
import io.ejat.framework.spi.ConfidentialTextException;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;

/**
 * <p>This tests class checks the behaviour of registering a local CPS using the FPF class is functional.</p>
 * 
 * @author James Davies
 */

public class FpfConfigurationPropertyStoreServiceTest {

    /**
     * <p>This test method checks the returned boolean from a class that checks if a URI is a local file. Expected true.</p>
     * @throws IOException
     */
    @Test
    public void testTheIsFileUriMethod() throws IOException{
        File testProp = File.createTempFile("ejatfpf_", ".properties");
        assertTrue("Return the incorrect scheme for the provided URI",FpfConfigurationPropertyStoreService.isFileUri(testProp.toURI()));
    }

    /**
     * <p>This test method checks the returned boolean from a class that checks if a URI is a local file. Expected false.</p>
     * @throws IOException
     */
    @Test
    public void testTheIsFileUriMethodWithUrl() throws URISyntaxException{
        URI uri = new URI("http://isthisevenreal.co.il.uk/nope");
        assertFalse("Return the incorrect scheme for the provided URI",FpfConfigurationPropertyStoreService.isFileUri(uri));
    }

    /**
     * <p> This method checks for no exceptions when intialising the Configuration Property Store as a FPF</p>
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testIntialise() throws ConfigurationPropertyStoreException, IOException{
        File testProp = File.createTempFile("ejatfpf_", ".properties");
        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(testProp.toURI()));
        assertTrue("Dummy",true);
    }

    /**
     * <p> This methof checks that the implemented get method works through this interface.</p>
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testGet() throws ConfigurationPropertyStoreException, IOException{
        File testProp = File.createTempFile("ejatfpf_", ".properties");

        Properties testProps = new Properties();

        testProps.setProperty("Test1", "SomeString");
        testProps.setProperty("Test2", "SomeString");
        testProps.setProperty("RootPasswordForEverySystemEver", "admin");
        testProps.setProperty("anotherString", "anotherString");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        String expected = "SomeString";

        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(testProp.toURI()));
        assertEquals("Did not return the expected value.", expected, fpfcps.getProperty("Test1"));
    }

    /**
     * <p>This method tests that the exception is caught is the local file is not there.</p>
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testException() throws ConfigurationPropertyStoreException, IOException{
        File file = new File("DefoNotAFile.com");
        boolean caught = false;
        try {
        FpfConfigurationPropertyStoreService fpfcps = new FpfConfigurationPropertyStoreService();
        fpfcps.initialise(new FrameworkInitialisation(file.toURI()));
        } catch (ConfigurationPropertyStoreException e){
            caught = true;
        }
        assertTrue("Exception not caught as expected.", caught);
    }

    /**
     * <p>This class is used to test the implemented methods in the tests above. They are all noddy methods.</p>
     */
    private class FrameworkInitialisation implements IFrameworkInitialisation {
        private URI uri;

        public FrameworkInitialisation(URI uri) {
            this.uri=uri;
        }

        @Override
        public void registerConfidentialTextService(@NotNull IConfidentialTextService cts) throws ConfidentialTextException{
        }
        @Override
        public URI getBootstrapConfigurationPropertyStore() {return uri;}
        @Override
        public void registerDynamicStatusStoreService(IDynamicStatusStoreService dynamicStatusStoreService){}
        @Override
        public IFramework getFramework(){return null;}
        
        @Override
        public void registerConfigurationPropertyStoreService(@NotNull IConfigurationPropertyStoreService configurationPropertyStoreService)
                throws ConfigurationPropertyStoreException {
        }

		@Override
		public URI getDynamicStatusStoreUri() {return null;}

		@Override
		public List<URI> getResultArchiveStoreUris() {return null;}

		@Override
		public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService) {
		}
    }

}