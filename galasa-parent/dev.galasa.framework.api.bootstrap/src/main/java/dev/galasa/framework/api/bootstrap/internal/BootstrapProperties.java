/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.bootstrap.internal;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;

public class BootstrapProperties {

    private IFramework               framework;  

    private final Properties        configurationProperties = new Properties();
    
    private ArrayList<String> bootstrapKeys;

    public BootstrapProperties (IFramework framework, ArrayList<String> bootstrapKeys){ 
        this.framework = framework;
        this.bootstrapKeys = bootstrapKeys;
    }

    public Properties getProperties() throws ConfigurationPropertyStoreException{
        getLatestPropertiesFromFramework();
        Properties bootstrapProperties = new Properties();
        synchronized (this.configurationProperties) {
            bootstrapProperties.putAll(this.configurationProperties);
        }
        return bootstrapProperties;
    }

    private void getLatestPropertiesFromFramework() throws ConfigurationPropertyStoreException {
        Map<String,String> properties = framework.getConfigurationPropertyService("bootstrap").getAllProperties();
        synchronized (configurationProperties) {
            for (String key : bootstrapKeys) {
                String value = (String) properties.get(key);
                if (value != null) {
                    this.configurationProperties.put(key, value);
                } else {
                    this.configurationProperties.remove(key);
                }
            }
        }
    }
    
}
