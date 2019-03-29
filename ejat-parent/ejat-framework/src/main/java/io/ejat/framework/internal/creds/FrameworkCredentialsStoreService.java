package io.ejat.framework.internal.creds;

import java.util.Properties;

import javax.crypto.IllegalBlockSizeException;

import io.ejat.framework.spi.creds.ICredentialsStore;
import io.ejat.framework.spi.creds.FileCredentialsToken;
import io.ejat.framework.spi.creds.FileCredentialsUsername;
import io.ejat.framework.spi.creds.FileCredentialsUsernamePassword;
import io.ejat.framework.spi.creds.ICredentialsStoreService;
import io.ejat.framework.spi.creds.ICredentials;
import io.ejat.framework.spi.IConfigurationPropertyStore;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.FrameworkInitialisation;
import io.ejat.framework.internal.cts.FrameworkConfidentialTextService;

public class FrameworkCredentialsStoreService implements ICredentialsStoreService {
    private IConfigurationPropertyStore cpsStore;
    //private String namespace;
    private String credsLocation = "~/.ejat/credentials.properties";
    private ICredentialsStore credsStore;
    private FrameworkConfidentialTextService confTextService;
    private Properties overrides;

    public FrameworkCredentialsStoreService(IFramework framework, IConfigurationPropertyStore cpsStore, ICredentialsStore credsStore, Properties overrides) {
        this.cpsStore = cpsStore;
        //this.namespace = namespace;
        this.credsStore = credsStore;
        this.overrides = overrides;

        try {
            String credsLocation = cpsStore.getProperty("framework.credentials.store");
            if (!credsLocation.equals(null)) {
                this.credsLocation = credsLocation;
            }

            if (cpsStore.getProperty("framework.credentials.auto.register.cts").equals("true")) {
                confTextService = new FrameworkConfidentialTextService();
    
                // Don't know what properties should be
                Properties bootstrapProperties = new Properties();
                confTextService.initialise(new FrameworkInitialisation(bootstrapProperties, overrides));
                
                //Don't know how to access the token and password to register them in the confidential text store as don't have credentialsId
            }
        } catch (Exception e) {

        }
        
    }

    @Override
    public ICredentials getCredentials(String credsId) throws CredentialsStoreException, IllegalBlockSizeException {
        return credsStore.getCredentials(credsId);
    }
}