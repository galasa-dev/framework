/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

@Component(service = { BackupCPS.class })
public class BackupCPS {
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    private StringBuilder   sb;
    private Path            path;
    
    private IFramework framework;
    
    /**
     * <p>Retrieves CPS properties for all configured namespaces and sends them to standard output</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @param filePath
     * @return
     * @throws FrameworkException
     */
    public void backup(Properties bootstrapProperties, Properties overrideProperties, String filePath) throws FrameworkException {
        
        logger.info("Initialising CPS Backup Service");
        
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }
        
        framework = frameworkInitialisation.getFramework();
        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");
        
        List<String> namespaces = cps.getCPSNamespaces(); 
        
        logger.info("Backing-up to file: " + filePath);
        
        initialiseFileOutput(filePath);
        
        outputCPSProperties(namespaces);
        
        logger.info("Ending CPS Backup Service");
        
        frameworkInitialisation.shutdownFramework();
        
    }
    
    /**
     * <p>Takes a list of namespaces and outputs CPS properties for each namespace.</p>
     * 
     * @param namespaces
     * @param framework
     * @return
     * @throws FrameworkException
     */  
    private void outputCPSProperties(List<String> namespaces) throws FrameworkException {
        
        sb = new StringBuilder();
        
        logger.info("Backing Up Namespaces:");
        
        java.util.Collections.sort(namespaces, java.text.Collator.getInstance());
        
        for (String namespace : namespaces) {
            if (isNamespaceBackupPermitted(namespace)) {
                logger.info("SUCCESS:\t" + namespace);
                outputNamespaceCPSProperties(namespace);
            } else {
                logger.info("FORBIDDEN:\t" + namespace);
            }
        }
        try {
            Files.write(path, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to save CPS properties: ", e);
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
    private void outputNamespaceCPSProperties(String namespace) throws FrameworkException {

        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(namespace);
        Map<String, String> properties = new TreeMap<>(cps.getAllProperties());
        
        if(sb.length()>0) {
            // Insert blank line between namespaces
            sb.append(System.lineSeparator());
        }
        
        for (Map.Entry<String, String> prop : properties.entrySet()) {
            sb.append(prop.getKey());
            sb.append("=");
            sb.append(prop.getValue());
            sb.append(System.lineSeparator());
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
        
        for(String ns : forbiddenNamespaces) {
            if (namespace.equals(ns)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * <p>Initialises path and creates files/directories necessary for output.</p>
     *
     * @param filePath
     * @return 
     * @throws FrameworkException 
     */ 
    private void initialiseFileOutput(String filePath) throws FrameworkException {
    	
    	path = Paths.get(filePath);
    	
    	try {
			Files.createFile(path);
			logger.info("File created: " + path.toUri().toString());
		} catch (FileAlreadyExistsException  e) {
			logger.info("Overwriting existing file: " + path.toUri().toString());
		} catch (IOException e) {
			throw new FrameworkException("Unable to create new file", e);
		}

    }
}