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

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

@Component(service = { BackupCPS.class })
public class BackupCPS {
    
    private Log  logger  =  LogFactory.getLog(this.getClass());
    
    /**
     * <p>Retrieves CPS properties for all configured namespaces and sends them to standard output</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @return
     * @throws FrameworkException
     */    
    public void backup(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {
    	
        logger.info("Initialising CPS Backup Service");
        
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Services", e);
        }
        
        IFramework framework = frameworkInitialisation.getFramework();
        
        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");
        List<String> namespaces = cps.getCPSNamespaces();      
        
        outputCPSProperties(namespaces, framework);
        
        logger.info("Ending CPS Backup Service");
        
    }
    
    /**
     * <p>Takes a list of namespaces and outputs CPS properties for each namespace.</p>
     * 
     * @param namespaces
     * @param framework
     * @return
     * @throws FrameworkException
     */  
    private void outputCPSProperties(List<String> namespaces, IFramework framework) throws FrameworkException {
    	logger.info("Backing Up Namespaces:");
    	for (String namespace : namespaces) {
    		if (isNamespaceBackupPermitted(namespace)) {
    			logger.info("SUCCESS:\t" + namespace);
    			//outputNamespaceCPSProperties(namespace, framework);
    		} else {
    			logger.info("FORBIDDEN:\t" + namespace);
    		}
    	}
    }
    
    /**
     * <p>Outputs CPS properties for a specified namespace.</p>
     * 
     * @param namespace
     * @param framework
     * @return
     * @throws FrameworkException
     */  
    private void outputNamespaceCPSProperties(String namespace, IFramework framework) throws FrameworkException {
    	IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(namespace);
		Map<String, String> properties = cps.getAllProperties();
		
		for (Map.Entry<String, String> prop : properties.entrySet()) {
			// Direct properties to standard output (for piping, etc.)
    		System.out.println(prop.getKey() + "=" + prop.getValue());
		}
    }
    
    /**
     * <p>Tests namespace for backup permission.</p>
     * <ul>
     *     <li>Returns true for valid namespace (backup permitted).</li>
     *     <li>Returns false for invalid namespace (backup not permitted).</li>
     * </ul>
     * 
     * @param namespace
     * @return boolean
     */ 
    private boolean isNamespaceBackupPermitted(String namespace) {
    	List<String> forbiddenNamespaces = new ArrayList<String>();
    	forbiddenNamespaces.add("dss");
    	forbiddenNamespaces.add("certificate");
    	forbiddenNamespaces.add("secure");
    	
    	// TODO - Change checking to whole word
    	for(String ns : forbiddenNamespaces) {
    		if (namespace.equals(ns)) {
    			return false;
    		}
    	}
    	return true;
    }

}
