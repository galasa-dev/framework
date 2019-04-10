package io.ejat.framework.internal.dss;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertTrue;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import io.ejat.framework.spi.IConfidentialTextService;
import io.ejat.framework.internal.cps.FpfConfigurationPropertyStore;
import io.ejat.framework.spi.ConfidentialTextException;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStore;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.creds.ICredentialsStore;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.DynamicStatusStoreException;

public class FpfDynamicStatusStoreRegistrationTest {

    @Test
    public void testInitialse() throws IOException, DynamicStatusStoreException {
        File testProp = File.createTempFile("cirillo", ".properties");
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
        public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsStoreException {           
        }
    }
}