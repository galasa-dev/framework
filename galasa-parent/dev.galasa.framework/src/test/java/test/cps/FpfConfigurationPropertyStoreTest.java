/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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

import dev.galasa.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.galasa.framework.spi.CertificateStoreException;
import dev.galasa.framework.spi.ConfidentialTextException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;

/**
 * <p>
 * This tests class checks the behaviour of registering a local CPS using the
 * FPF class is functional.
 * </p>
 * 
 * @author James Davies
 */

public class FpfConfigurationPropertyStoreTest {

    /**
     * <p>
     * This test method checks the returned boolean from a class that checks if a
     * URI is a local file. Expected true.
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testTheIsFileUriMethod() throws IOException {
        File testProp = File.createTempFile("galasafpf_", ".properties");
        assertTrue("Return the incorrect scheme for the provided URI",
                FpfConfigurationPropertyStore.isFileUri(testProp.toURI()));
    }

    /**
     * <p>
     * This test method checks the returned boolean from a class that checks if a
     * URI is a local file. Expected false.
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testTheIsFileUriMethodWithUrl() throws URISyntaxException {
        URI uri = new URI("http://isthisevenreal.co.il.uk/nope");
        assertFalse("Return the incorrect scheme for the provided URI", FpfConfigurationPropertyStore.isFileUri(uri));
    }

    /**
     * <p>
     * This methof checks that the implemented get method works through this
     * interface.
     * </p>
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testGet() throws ConfigurationPropertyStoreException, IOException {
        File testProp = File.createTempFile("galasafpf_", ".properties");

        Properties testProps = new Properties();

        testProps.setProperty("Test1", "SomeString");
        testProps.setProperty("Test2", "SomeString");
        testProps.setProperty("RootPasswordForEverySystemEver", "admin");
        testProps.setProperty("anotherString", "anotherString");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        String expected = "SomeString";

        FpfConfigurationPropertyStore fpfCps = new FpfConfigurationPropertyStore(testProp.toURI());

        assertEquals("Did not return the expected value.", expected, fpfCps.getProperty("Test1"));
    }

    /**
     * <p>
     * This method tests that the exception is caught is the local file is not
     * there.
     * </p>
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testException() throws ConfigurationPropertyStoreException, IOException {
        File file = new File("DefoNotAFile.com");
        boolean caught = false;
        try {
            FpfConfigurationPropertyStore fpfCps = new FpfConfigurationPropertyStore(file.toURI());
        } catch (ConfigurationPropertyStoreException e) {
            caught = true;
        }
        assertTrue("Exception not caught as expected.", caught);
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

        public FrameworkInitialisation(URI uri) {
            this.uri = uri;
            try {
                fpf = new FpfConfigurationPropertyStore(uri);
            } catch (ConfigurationPropertyStoreException e) {

            }
        }

        @Override
        public void registerConfidentialTextService(@NotNull IConfidentialTextService cts)
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
            return null;
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
            return null;
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

}
