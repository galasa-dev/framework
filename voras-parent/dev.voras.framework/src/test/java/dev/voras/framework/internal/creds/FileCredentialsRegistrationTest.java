package dev.voras.framework.internal.creds;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.validation.constraints.NotNull;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

import dev.voras.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.voras.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.voras.framework.internal.creds.FileCredentialsRegistration;
import dev.voras.framework.spi.ConfidentialTextException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.DynamicStatusStoreException;
import dev.voras.framework.spi.FrameworkException;
import dev.voras.framework.spi.IConfidentialTextService;
import dev.voras.framework.spi.IConfigurationPropertyStore;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IDynamicStatusStore;
import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IFrameworkInitialisation;
import dev.voras.framework.spi.IFrameworkRuns;
import dev.voras.framework.spi.IResourcePoolingService;
import dev.voras.framework.spi.IResultArchiveStore;
import dev.voras.framework.spi.IResultArchiveStoreService;
import dev.voras.framework.spi.IRun;
import dev.voras.framework.spi.creds.CredentialsException;
import dev.voras.framework.spi.creds.ICredentialsService;
import dev.voras.framework.spi.creds.ICredentialsStore;

/**
 * <p>This test class checks the behaviour of registering a local Credentials Store.</p>
 * 
 * @author Bruce Abbott
 */
public class FileCredentialsRegistrationTest {

    private Properties bootstrap;

    /**
     * <p>This test method checks that a local Credentials Store can be registered and initialised.</p>
     * @throws IOException
     * @throws CredentialsStoreException
     * @throws URISyntaxException
     * @throws InvalidSyntaxException
     * @throws FrameworkException
     */
    @Test
    public void testInitialise() throws IOException, CredentialsException, URISyntaxException, InvalidSyntaxException, FrameworkException {
        File testCreds = File.createTempFile("cirillo", ".properties");
        FileCredentialsRegistration fileCredsReg = new FileCredentialsRegistration();

        bootstrap = new Properties();
        bootstrap.setProperty("framework.config.store", "");

        FrameworkInitialisation fi = new FrameworkInitialisation(testCreds.toURI());
        fileCredsReg.initialise(fi);
        assertTrue("Dummy", true);
    }

    /**
     * <p>This class is used to test the implemented methods in the tests above. They are all noddy methods.</p>
     */
    private class FrameworkInitialisation implements IFrameworkInitialisation {
        private URI uri;
        private FpfConfigurationPropertyStore fpf;
        private IFramework framework;
        private Properties overrides;
        private Properties records;

        public FrameworkInitialisation(URI uri) {
            this.framework = new Framework();
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
        public IFramework getFramework(){
            return this.framework;
        }
        
        @Override
        public void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore)
                throws ConfigurationPropertyStoreException {
        }

		@Override
        public URI getDynamicStatusStoreUri() {return null;}
        
        @Override
		public URI getCredentialsStoreUri() {
            System.out.println(uri);
            return uri;
        }

		@Override
		public List<URI> getResultArchiveStoreUris() {return null;}

		@Override
		public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService) {
        }
        
        @Override
        public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsException {           
        }
    }

    /**
     * <p>This is a private class used to implement the IFramework for testing purposes.</p>
     */
    private class Framework implements IFramework{
        private Properties overrides = new Properties();
        private Properties records = new Properties();

        public IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace) throws ConfigurationPropertyStoreException {
            FrameworkConfigurationPropertyService fcps;
            try {
                File testFile = File.createTempFile("test", ".properties");
                Framework framework = new Framework();
                
                fcps = new FrameworkConfigurationPropertyService(framework, new FpfConfigurationPropertyStore(testFile.toURI()) , overrides, records, "framework");
                return fcps;
                
            } catch (Exception e) {
                System.out.println("Exception");
            }
            
            return null;  
        }

        public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace) throws DynamicStatusStoreException {
            return null;
        }
        public IResultArchiveStore getResultArchiveStore(){return null;}
        public IResourcePoolingService getResourcePoolingService(){return null;}

		@Override
		public @NotNull IConfidentialTextService getConfidentialTextService() {return null;}

		@Override
		public String getTestRunName() {
			return null;
        }

        @Override
        public ICredentialsService getCredentialsService() throws CredentialsException {
            return null;
        }

		@Override
		public Random getRandom() {
			return null;
		}

		@Override
		public IRun getTestRun() {
			return null;
		}

		@Override
		public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
			return null;
		}

		@Override
		public void setFrameworkProperties(Properties overrideProperties) {
		}

		@Override
		public boolean isInitialised() {
			return false;
		}

		@Override
		public void initialisationComplete() {
		}
		@Override
		public Properties getRecordProperties() {
			return null;
		}
      
    } 
}