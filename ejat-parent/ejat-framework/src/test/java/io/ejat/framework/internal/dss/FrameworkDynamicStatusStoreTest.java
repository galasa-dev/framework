package io.ejat.framework.internal.dss;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.FrameworkPropertyFileException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ResultArchiveStoreException;

/**
 * Test the Framework DSS Stub. Most of the functionality will exist in the
 * service, so this is just a quick drive of the stub code.
 *
 * @author Michael Baylis
 *
 */
public class FrameworkDynamicStatusStoreTest {

    private static final String         NAMESPACE = "testy";
    private static final String         PREFIX    = "dss." + NAMESPACE + ".";

    private Path                        tempProperties;
    private FrameworkPropertyFile       fpf;
    private FrameworkDynamicStatusStore dss;

    @Before
    public void setup() throws IOException, FrameworkPropertyFileException, DynamicStatusStoreException {
        this.tempProperties = Files.createTempFile("ejat_dss_junit", ".propertes");
        final FrameworkInitialisation frameworkInitialisation = new FrameworkInitialisation(
                this.tempProperties.toUri());
        this.fpf = new FrameworkPropertyFile(this.tempProperties.toUri());

        final FpfDynamicStatusStoreService dsss = new FpfDynamicStatusStoreService();
        dsss.initialise(frameworkInitialisation);
        this.dss = new FrameworkDynamicStatusStore(null, dsss, NAMESPACE);
    }

    @After
    public void teardown() throws IOException {
        if ((this.tempProperties != null) && Files.exists(this.tempProperties)) {
            Files.delete(this.tempProperties);
        }
    }

    @Test
    public void testSimpleMethods() throws DynamicStatusStoreException {
        final String key = "a_little_key";
        final String value = UUID.randomUUID().toString();

        this.dss.put(key, value);
        delayForFileTimestampChange();
        Assert.assertEquals("Key values differ", value, this.fpf.get(PREFIX + key));
        Assert.assertEquals("Key values differ", value, this.dss.get(key));

        this.dss.delete(key);
        delayForFileTimestampChange();
        Assert.assertNull("Should have gone", this.fpf.get(PREFIX + key));
    }

    @Test
    public void testMapMethods() throws DynamicStatusStoreException {
        final HashMap<String, String> map = new HashMap<>();

        final String key1 = "a_little_key";
        final String value1 = UUID.randomUUID().toString();
        final String key2 = "a_tiny_key";
        final String value2 = UUID.randomUUID().toString();

        map.put(key1, value1);
        map.put(key2, value2);

        this.dss.put(map);

        delayForFileTimestampChange();
        Assert.assertEquals("Key values differ", value1, this.fpf.get(PREFIX + key1));
        Assert.assertEquals("Key values differ", value2, this.fpf.get(PREFIX + key2));

        this.dss.delete(map.keySet());
        delayForFileTimestampChange();
        Assert.assertNull("Should have gone", this.fpf.get(PREFIX + key1));
        Assert.assertNull("Should have gone", this.fpf.get(PREFIX + key2));
    }

    @Test
    public void testPrefixMethods() throws DynamicStatusStoreException {
        final String key1 = "a_little_key";
        final String value1 = UUID.randomUUID().toString();
        final String key2 = "a_tiny_key";
        final String value2 = UUID.randomUUID().toString();

        this.dss.put(key1, value1);
        this.dss.put(key2, value2);

        final Map<String, String> values = this.dss.getPrefix("a_");

        delayForFileTimestampChange();
        Assert.assertEquals("Key values differ", value1, values.get(key1));
        Assert.assertEquals("Key values differ", value2, values.get(key2));
        Assert.assertEquals("Key values differ", value1, this.fpf.get(PREFIX + key1));
        Assert.assertEquals("Key values differ", value2, this.fpf.get(PREFIX + key2));

        this.dss.deletePrefix("a_");
        delayForFileTimestampChange();
        Assert.assertNull("Should have gone", this.fpf.get(PREFIX + key1));
        Assert.assertNull("Should have gone", this.fpf.get(PREFIX + key2));
    }

    @Test
    public void testSwapMethods() throws DynamicStatusStoreException {
        final String key1 = "a_little_key";
        final String value1a = UUID.randomUUID().toString();
        final String value1b = UUID.randomUUID().toString();

        final String key2 = "a_tiny_key";
        final String value2a = UUID.randomUUID().toString();
        final String value2b = UUID.randomUUID().toString();
        final HashMap<String, String> map = new HashMap<>();
        map.put(key2, value2a);

        // *** Disabled due to Issue framework#128
        // Assert.assertTrue("Initial swap should work", dss.putSwap(key1, null,
        // value1a));
        // Assert.assertEquals("Key values differ", value1a, fpf.get(PREFIX + key1));
        // Assert.assertFalse("2nd swap should false", dss.putSwap(key1, null,
        // value1a));

        // *** Temporary until 128 fixed
        {
            this.dss.put(key1, value1b);
            Assert.assertTrue("Initial swap should work", this.dss.putSwap(key1, value1b, value1a));
            delayForFileTimestampChange();
            Assert.assertEquals("Key values differ", value1a, this.fpf.get(PREFIX + key1));
            Assert.assertFalse("2nd swap should false", this.dss.putSwap(key1, value1b, value1a));
        }

        Assert.assertTrue("3rd swap should work", this.dss.putSwap(key1, value1a, value1b));
        delayForFileTimestampChange();
        Assert.assertEquals("Key values differ", value1b, this.fpf.get(PREFIX + key1));

        Assert.assertTrue("1st Map swap should work", this.dss.putSwap(key1, value1b, value1a, map));
        delayForFileTimestampChange();
        Assert.assertEquals("Key values differ", value1a, this.fpf.get(PREFIX + key1));
        Assert.assertEquals("Key values differ", value2a, this.fpf.get(PREFIX + key2));

        map.put(key2, value2b);
        Assert.assertFalse("2nd Map swap should false", this.dss.putSwap(key1, value1b, value1a, map));
        delayForFileTimestampChange();
        Assert.assertEquals("Key values differ", value1a, this.fpf.get(PREFIX + key1));
        Assert.assertEquals("Key values differ", value2a, this.fpf.get(PREFIX + key2));

    }

    @Test
    public void testFutureDynamicResource() throws DynamicStatusStoreException {
        Assert.assertNotNull("Should get a dynamic resource", this.dss.getDynamicResource("bob"));
    }

    @Test
    public void testFutureDynamicRun() throws DynamicStatusStoreException {
        Assert.assertNotNull("Should get a dynamic run", this.dss.getDynamicRun());
    }


    private void delayForFileTimestampChange() {
        await().atMost(1, TimeUnit.SECONDS).until(fpfWriteTimeDifferent());
    }

    private Callable<Boolean> fpfWriteTimeDifferent() {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                FileTime time = Files.getLastModifiedTime(tempProperties);
                return (time.toMillis() != System.currentTimeMillis());
            }
        };
    }

    /**
     * <p>
     * This class is used to test the implemented methods in the tests above. They
     * are all noddy methods.
     * </p>
     */
    private class FrameworkInitialisation implements IFrameworkInitialisation {
        private final URI uri;

        public FrameworkInitialisation(URI uri) {
            this.uri = uri;
        }

        @Override
        public URI getBootstrapConfigurationPropertyStore() {
            return null;
        }

        @Override
        public void registerDynamicStatusStoreService(IDynamicStatusStoreService dynamicStatusStoreService) {
        }

        @Override
        public IFramework getFramework() {
            return null;
        }

        @Override
        public void registerConfigurationPropertyStoreService(
                @NotNull IConfigurationPropertyStoreService configurationPropertyStoreService)
                        throws ConfigurationPropertyStoreException {
        }

        @Override
        public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService)
                throws ResultArchiveStoreException {
        }

        @Override
        public URI getDynamicStatusStoreUri() {
            return this.uri;
        }

        @Override
        public List<URI> getResultArchiveStoreUris() {
            return null;
        }

    }
}