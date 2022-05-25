/*
 * Copyright contributors to the Galasa project
 */
package test.cts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import dev.galasa.framework.internal.cts.FrameworkConfidentialTextService;
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
 * This test class ensures that confidential texts that have been registered are
 * reomved from text.
 * 
 * @author James Davies
 */
public class ConfidentialTextServiceTest {

    /**
     * The test method adds a confidential text to the service.
     * 
     * @throws ConfidentialTextException
     * @throws IOException
     */
    @Test
    public void testRegisterText() throws ConfidentialTextException, IOException {
        FrameworkConfidentialTextService ctsService = new FrameworkConfidentialTextService();

        ctsService.registerText("test1", "This is a test comment");
        assertTrue("dummy", true);
    }

    /**
     * This test method ensures that any regitered words or phrases are removed from
     * a text.
     * 
     * @throws ConfidentialTextException
     * @throws IOException
     */
    @Test
    public void testRemoveConfidentialText() throws ConfidentialTextException, IOException {
        FrameworkConfidentialTextService ctsService = new FrameworkConfidentialTextService();

        ctsService.registerText("test1", "This is a test comment");
        ctsService.registerText("test2", "This is a test comment");
        ctsService.registerText("test3", "This is a test comment");

        String testSentence = "The current password is test1, the old password is test3, and the new password is test2";
        String expected = "The current password is **1**, the old password is **3**, and the new password is **2**";

        String result = ctsService.removeConfidentialText(testSentence);
        System.out.println(result + "\n" + expected);
        assertEquals("Did not remove confidential imfomation ", expected, result);
    }

    /**
     * This test method ensures that any regitered words or phrases are removed from
     * a text.
     * 
     * @throws ConfidentialTextException
     * @throws IOException
     */
    @Test
    public void testRemoveConfidentialTextWithDollarSymbol() throws ConfidentialTextException, IOException {
        FrameworkConfidentialTextService ctsService = new FrameworkConfidentialTextService();

        ctsService.registerText("test1", "This is a test comment");
        ctsService.registerText("te$t2", "This is a test comment");
        ctsService.registerText("test3", "This is a test comment");

        String testSentence = "The current password is test1, the old password is test3, and the new password is te$t2";
        String expected = "The current password is **1**, the old password is **3**, and the new password is **2**";

        String result = ctsService.removeConfidentialText(testSentence);
        System.out.println(result + "\n" + expected);
        assertEquals("Did not remove confidential imfomation ", expected, result);
    }

    /**
     * This is a stubbed framework intialisation class to test the registering of
     * the service.
     */
    private class FrameworkInitialisation implements IFrameworkInitialisation {
        private URI uri;

        public FrameworkInitialisation(URI uri) {
            this.uri = uri;
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
