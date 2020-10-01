/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

@Component(service = { BackupCPS.class })
public class BackupCPS {
    
    private Log  logger  =  LogFactory.getLog(this.getClass());
    
    public void backup(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {

        logger.info("Initialising CPS Backup Service");

        // *** Initialise the framework services
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Services", e);
        }
        IFramework framework = frameworkInitialisation.getFramework();
        
        // *** Retrieve CPS properties and output to console.
        Map<String, String> properties = framework
        		.getConfigurationPropertyService("framework")
        		.getAllProperties();
        
        for (Map.Entry<String, String> prop : properties.entrySet()) {
        	if(isValidProperty(prop.getKey())) {
        		logger.info(prop.getKey() + " = " + prop.getValue());
        		System.out.println(prop.getKey() + " = " + prop.getValue());
        	}
        }
        
    }
    
    private boolean isValidProperty(String property) {
    	List<String> forbiddenPrefixes = new ArrayList<String>();
    	forbiddenPrefixes.add("dss.");
    	forbiddenPrefixes.add("certificate.");
    	forbiddenPrefixes.add("secure.");
    	
    	for(String prefix : forbiddenPrefixes) {
    		if (property.startsWith(prefix)) {
    			return false;
    		}
    	}
    	return true;
    }

}
