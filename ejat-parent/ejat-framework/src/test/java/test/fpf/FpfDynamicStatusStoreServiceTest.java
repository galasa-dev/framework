package test.fpf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import io.ejat.framework.internal.dss.FpfDynamicStatusStoreService;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;

/**
 * <p>This test class checks the behaviour of registering a local DSS using the FPF class in functional</p>
 * 
 * @author Bruce Abbott
 */

 public class FpfDynamicStatusStoreServiceTest {

    /**
     * <p>This test checks the returned boolean from a class that checks if a URI is a local file. Expected False.</p>
     * @throws URISyntaxException
     */
    @Test
    public void testTheIsFileUriMethodWithUrl() throws URISyntaxException {
        URI uri = new URI("http://isthisevenreal.co.il.uk/nope");
        assertFalse("Return the incorrect scheme for the provided URI", FpfDynamicStatusStoreService.isFileUri(uri));
    }

    /**
     * <p>This test checks the returned boolean from a class that checks if a URI is a local file, Expected True.</p>
     * @throws IOException
     */
    @Test
    public void testTheIsFileUriMethodWithLocalFile() throws IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        assertTrue("Return the incorrect scheme for the provided URI", FpfDynamicStatusStoreService.isFileUri(testFile.toURI()));
    }

    /**
     * <p>This test checks for no exceptions when initialising the DSS as an FPF</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testInitialise() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();       
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        assertTrue("Exception during initialisation", true);
    }

    /**
     * <p>This test checks for no exceptions when putting a key/value pair into the DSS</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutSingle() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("testKey", "testValue");
        assertTrue("Exception during put of single key/value pair", true);
    }

    /**
     * <p>This test checks for no exceptions when putting a map of multiple key/value pairs into the DSS</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test 
    public void testPutMultiple() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        Map keyValuePairs = new HashMap();
        keyValuePairs.put("key1", "value1");
        keyValuePairs.put("key2", "value2");
        fpfdss.put(keyValuePairs);
        assertTrue("Exception during put of multiple key/value pairs", true);
    }

    /**
     * <p>This test checks the returned boolean from attempting a swap which has already occured. Expected False.</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test 
    public void testPutSwap1() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("testKey", "testValue2");
        assertFalse("Swap occured when not required", fpfdss.putSwap("testKey", "testValue1", "testValue2"));
    }

    /**
     * <p>This test checks the returned boolean from attempting a swap. Expected True.</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test 
    public void testPutSwap2() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("testKey", "testValue1");
        assertTrue("Swap did not successfully occur", fpfdss.putSwap("testKey", "testValue1", "testValue2"));
    }

    /**
     * <p>This test checks the returned boolean from attempting a swap (and putting multiple key/value pairs from a Map) which 
     *  has already occured. Expected False.</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutSwapMultiple1() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("testKey", "testValue2");
        Map keyValuePairs = new HashMap();
        keyValuePairs.put("key1", "value1");
        keyValuePairs.put("key2", "value2");
        assertFalse("Swap occured when not required", fpfdss.putSwap("testKey", "testValue1", "testValue2", keyValuePairs));
    }

    /**
     * <p>This test checks the returned boolean from attempting a swap and put of a Map of key/value pairs. Expected True.</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutSwapMultiple2() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("testKey", "testValue1");
        Map keyValuePairs = new HashMap();
        keyValuePairs.put("key1", "value1");
        keyValuePairs.put("key2", "value2");
        assertTrue("Swap did not successfully occur", fpfdss.putSwap("testKey", "testValue1", "testValue2", keyValuePairs));
    }

    /**
     * <p>This test checks that a value can be retrieved.</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testGet() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("testKey", "testValue");
        assertEquals("Incorrect value retrieved", fpfdss.get("testKey"), "testValue");
    }

    /**
     * <p>This test checks if all key/value pairs with a common prefix can be retrieved.</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testGetPrefix() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("prefix.infix1.suffix", "testValue1");
        fpfdss.put("prefix.infix2.suffix", "testValue2");
        Map keyValuePairs = new HashMap();
        keyValuePairs.put("prefix.infix1.suffix", "testValue1");
        keyValuePairs.put("prefix.infix2.suffix", "testValue2");
        assertEquals("Incorrect values retrieved", keyValuePairs, fpfdss.getPrefix("prefix"));
    }

    /**
     * <p>This test checks if a key/value pair can be deleted.</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testDelete() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("testKey", "testValue");
        fpfdss.delete("testKey");
        assertNull("Key/value pair not deleted successfully", fpfdss.get("testKey"));
    }

    /**
     * <p>This test checks if all key/value pairs with a common prefix can be deleted.</p>
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testDeletePrefix() throws DynamicStatusStoreException, IOException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        FpfDynamicStatusStoreService fpfdss = new FpfDynamicStatusStoreService();  
        fpfdss.initialise(new FrameworkInitialisation(testFile.toURI()));
        fpfdss.put("prefix.infix1.suffix", "testValue1");
        fpfdss.put("prefix.infix2.suffix", "testValue2");
        fpfdss.deletePrefix("prefix");
        assertEquals("Key/value pairs not deleted successfully", new HashMap<String, String>(), fpfdss.getPrefix("prefix"));
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
        public URI getBootstrapConfigurationPropertyStore() {return null;}
        @Override
        public void registerDynamicStatusStoreService(IDynamicStatusStoreService dynamicStatusStoreService){}
        @Override
        public IFramework getFramework(){return null;}
        
        @Override
        public void registerConfigurationPropertyStoreService(@NotNull IConfigurationPropertyStoreService configurationPropertyStoreService)
                throws ConfigurationPropertyStoreException {
        }

		@Override
		public URI getDynamicStatusStoreUri() {return uri;}

		@Override
		public List<URI> getResultArchiveStoreUris() {return null;}

		@Override
		public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService) {
		}
    }

 }