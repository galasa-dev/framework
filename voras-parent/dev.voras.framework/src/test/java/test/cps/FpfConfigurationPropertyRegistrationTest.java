package test.cps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import dev.voras.framework.internal.cps.FpfConfigurationPropertyRegistration;
import dev.voras.framework.internal.cps.FpfConfigurationPropertyStore;
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

public class FpfConfigurationPropertyRegistrationTest {

    @Test
    public void testIntialise() throws IOException, ConfigurationPropertyStoreException{
        File testProp = File.createTempFile("cirillo", ".properties");
        FpfConfigurationPropertyRegistration fpfCpsReg = new FpfConfigurationPropertyRegistration();
        fpfCpsReg.initialise(new FrameworkInitialisation(testProp.toURI()));
        assertTrue("Dummy",true);
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
        public URI getBootstrapConfigurationPropertyStore() {return uri;}
        @Override
        public void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore) throws DynamicStatusStoreException{}
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