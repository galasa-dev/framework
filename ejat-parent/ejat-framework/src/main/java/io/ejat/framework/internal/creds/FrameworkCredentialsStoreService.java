package io.ejat.framework.internal.creds;

import java.util.Properties;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.creds.ICredentialsStore;
import io.ejat.framework.spi.creds.ICredentialsStoreService;
import io.ejat.framework.spi.creds.ICredentials;
import io.ejat.framework.spi.IConfigurationPropertyStore;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.FrameworkInitialisation;
import io.ejat.framework.internal.cts.FrameworkConfidentialTextServiceRegistration;

import javax.crypto.IllegalBlockSizeException;

/**
 * <p>This class is used to drive the registered Credentials Store, and retireve values from the Credentials Store.</p>
 * 
 * @author Bruce Abbott
 */
public class FrameworkCredentialsStoreService implements ICredentialsStoreService {
    private IConfigurationPropertyStore cpsStore;
    private String credsLocation;
    private ICredentialsStore credsStore;
    private FrameworkConfidentialTextServiceRegistration confTextServiceRegistration;
    private Properties overrides;

     /**
     * <p>This constructor retrieves the location of stored credentials and registers credentials with the confidentials text store</p>
     * 
     * @param framework - not currently used.
     * @param cpsStore - the registered store for the CPS
     * @param credsStore - the registered store the the Credentials
     * @param overrides - property values to be selected as preference from these properties
     */
    public FrameworkCredentialsStoreService(IFramework framework, IConfigurationPropertyStore cpsStore, ICredentialsStore credsStore, Properties overrides) {
        this.cpsStore = cpsStore;
        this.credsStore = credsStore;
        this.overrides = overrides;

        try {
            String credsLocation = cpsStore.getProperty("framework.credentials.store");
            if (credsLocation != null) {
                this.credsLocation = credsLocation;
            }

            if (cpsStore.getProperty("framework.credentials.auto.register.cts").equals("true")) {
                confTextServiceRegistration = new FrameworkConfidentialTextServiceRegistration();
    
                Properties bootstrapProperties = new Properties();
                confTextServiceRegistration.initialise(new FrameworkInitialisation(bootstrapProperties, overrides));
                
                //Don't know how to access the token and password to register them in the confidential text store as don't have credentialsId
            }
        } catch (Exception e) {

        }
        
    }

    /**
	 * <p>A simple method thta checks the provided URI to the CPS is a local file or not.</p>
	 * 
	 * @param credsId - id used to access the credentials
	 * @return - object containing appropriate credentials
     * @throws CredentialsStoreException
     * @throws IllegalBlockSizeException
	 */
    @Override
    public ICredentials getCredentials(@NotNull String credsId) throws CredentialsStoreException, IllegalBlockSizeException {
        return credsStore.getCredentials(credsId);
    }
}