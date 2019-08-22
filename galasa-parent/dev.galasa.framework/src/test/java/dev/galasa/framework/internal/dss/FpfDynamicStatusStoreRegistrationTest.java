package dev.galasa.framework.internal.dss;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import dev.galasa.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.galasa.framework.internal.dss.FpfDynamicStatusStoreRegistration;
import dev.galasa.framework.spi.ConfidentialTextException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;

public class FpfDynamicStatusStoreRegistrationTest {

    @Test
    public void testInitialse() throws IOException, DynamicStatusStoreException {
        File testProp = File.createTempFile("galasa", ".properties");
        FpfDynamicStatusStoreRegistration fpfDssReg = new FpfDynamicStatusStoreRegistration();
        fpfDssReg.initialise(new FrameworkInitialisation(testProp.toURI()));
        assertTrue("Dummy", true);
    }
    
    /**
     * <p>This class is used to test the implemented methods in the tests above. They are all noddy methods.</p>
     */
    private class FrameworkInitialisation implements IFrameworkInitialisation {
        private URI uri;
        private FpfConfigurationPropertyStore fpf;

        public FrameworkInitialisation(URI uri) {
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
        public URI getBootstrapConfigurationPropertyStore() {return null;}
        @Override
        public void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore) throws DynamicStatusStoreException{}
        @Override
        public IFramework getFramework(){return null;}
        
        @Override
        public void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore)
                throws ConfigurationPropertyStoreException {
        }

		@Override
        public URI getDynamicStatusStoreUri() {return uri;}
        
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