package io.ejat.framework.internal.creds;

import java.util.Properties;

import io.ejat.framework.spi.creds.ICredentialsStore;
import io.ejat.framework.spi.creds.FileCredentialsToken;
import io.ejat.framework.spi.creds.FileCredentialsUsername;
import io.ejat.framework.spi.creds.FileCredentialsUsernamePassword;
import io.ejat.framework.spi.creds.ICredentialsStoreService;
import io.ejat.framework.spi.creds.ICredentials;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.FrameworkInitialisation;
import io.ejat.framework.internal.cts.FrameworkConfidentialTextService;

public class FrameworkCredentialsStoreService implements ICredentialsStore {
    private IConfigurationPropertyStoreService cpsService;
    private String namespace;
    private String credsLocation = "~/.ejat/credentials.properties";
    private ICredentialsStoreService credsService;
    private FrameworkConfidentialTextService confTextService;
    private Properties overrides;

    public FrameworkCredentialsStoreService(IFramework framework, IConfigurationPropertyStoreService cpsService, ICredentialsStoreService credsService, String namespace, Properties overrides)
            throws ConfigurationPropertyStoreException {
        this.cpsService = cpsService;
        this.namespace = namespace;
        this.credsService = credsService;
        this.overrides = overrides;

        String credsLocation = cpsService.getProperty("framework.credentials.store");
        if (!credsLocation.equals(null)) {
            this.credsLocation = credsLocation;
        }

        try {
            if (cpsService.getProperty("framework.credentials.auto.register.cts").equals("true")) {
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
    public ICredentials getCredentials(String credsId) throws CredentialsStoreException {
        return credsService.getCredentials(credsId);
    }
}