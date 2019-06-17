package test.cts;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import dev.voras.framework.internal.cts.FrameworkConfidentialTextServiceRegistration;
import dev.voras.framework.spi.ConfidentialTextException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.DynamicStatusStoreException;
import dev.voras.framework.spi.IConfidentialTextService;
import dev.voras.framework.spi.IConfigurationPropertyStore;
import dev.voras.framework.spi.IDynamicStatusStore;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IFrameworkInitialisation;
import dev.voras.framework.spi.IResultArchiveStoreService;
import dev.voras.framework.spi.creds.CredentialsException;
import dev.voras.framework.spi.creds.ICredentialsStore;

/**
 * This test class ensures that confidential texts that have been registered are reomved from text.
 * 
 * @author James Davies
 */
public class ConfidentialTextServiceRegistrationTest {

    /**
     * This method intialises the confidentialTextService and checks no exceptions are thrown.
     * 
     * @throws ConfidentialTextException - if the service cannot be registered (i.e more than 1 service).
     * @throws IOException
     */
    @Test
    public void testInitialise() throws ConfidentialTextException, IOException{
        FrameworkConfidentialTextServiceRegistration ctsService = new FrameworkConfidentialTextServiceRegistration();
        File testProp = File.createTempFile("ejat_", ".properties");
        ctsService.initialise(new FrameworkInitialisation(testProp.toURI()));
        assertTrue("dummy", true);
    }

        /**
     * This is a stubbed framework intialisation class to test the registering of the service.
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
        public void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore)
                throws DynamicStatusStoreException{}
        @Override
        public IFramework getFramework(){return null;}
        
        @Override
        public void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore)
                throws ConfigurationPropertyStoreException {
        }

		@Override
        public URI getDynamicStatusStoreUri() {return null;}
        
        @Override
		public URI getCredentialsStoreUri() {return null;}

		@Override
		public List<URI> getResultArchiveStoreUris() {return null;}

		@Override
		public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService) {
        }
        
        @Override
        public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsException {           
        }
    }
}