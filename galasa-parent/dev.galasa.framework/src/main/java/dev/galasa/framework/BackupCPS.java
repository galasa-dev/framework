/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
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

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

@Component(service = { BackupCPS.class })
public class BackupCPS {
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    private IFramework framework;
    
    /**
     * <p>Retrieves CPS properties for all configured namespaces and sends them to standard output</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @param filePath
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
        
        String cpsProperties = getCPSProperties(namespaces);
        outputToFile(filePath, cpsProperties);
        
        logger.info("Ending CPS Backup Service");
        
        frameworkInitialisation.shutdownFramework();
        
    }
    
    /**
     * <p>Takes a list of namespaces and returns CPS properties for each namespace.</p>
     * 
     * @param namespaces
     * @param framework
     * @return String
     * @throws FrameworkException
     */  
    private String getCPSProperties(List<String> namespaces) throws FrameworkException {
                
        logger.info("Backing Up Namespaces:");
        
        java.util.Collections.sort(namespaces, java.text.Collator.getInstance());
        
        StringBuilder sbAllNamespaceProps = new StringBuilder();
        
        for (String namespace : namespaces) {
            if (isNamespaceBackupPermitted(namespace)) {
                logger.info("SUCCESS:\t" + namespace);
                sbAllNamespaceProps.append(getNamespaceCPSProperties(namespace) + System.lineSeparator());
            } else {
                logger.info("FORBIDDEN:\t" + namespace);
            }
        }
        
        return sbAllNamespaceProps.toString();
        
    }
    
    /**
     * Outputs the specified message to the filePath specified.
     * 
     * @param filePath
     * @param message
     * @throws FrameworkException
     */
    private void outputToFile(String filePath, String message) throws FrameworkException {
        Path path = Paths.get(filePath);
        
        Path pathParent = path.getParent();
        
        if (pathParent != null) {
            try {
                if (!Files.exists(pathParent)) {
                    Files.createDirectories(pathParent);
                    logger.info("Created directory: " + pathParent.toString());
                }
            } catch (IOException e) {
                throw new FrameworkException("Failed to create directory: " + pathParent.toString(), e);
            }
        }
        
        try {
            Files.write(path, message.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            logger.info("Written backup to: " + path.toString());
        } catch (IOException e1) {
            throw new FrameworkException("Failed to write to file: " + path.toString(), e1);
        }
    }
    
    /**
     * <p>Returns CPS properties for a specified namespace.</p>
     * 
     * @param namespace
     * @param framework
     * @return String
     * @throws FrameworkException
     */  
    private String getNamespaceCPSProperties(String namespace) throws FrameworkException {

        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(namespace);
        Map<String, String> properties = new TreeMap<>(cps.getAllProperties());
        
        StringBuilder sbNamespaceProps = new StringBuilder();
        
        for (Map.Entry<String, String> prop : properties.entrySet()) {
            sbNamespaceProps.append(prop.getKey());
            sbNamespaceProps.append("=");
            sbNamespaceProps.append(prop.getValue());
            sbNamespaceProps.append(System.lineSeparator());
        }
        return sbNamespaceProps.toString();
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
        List<String> forbiddenNamespaces = new ArrayList<>();
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

}