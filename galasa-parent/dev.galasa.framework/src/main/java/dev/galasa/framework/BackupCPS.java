/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
        
        IFramework framework = frameworkInitialisation.getFramework();
        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");
        
        List<String> namespaces = cps.getCPSNamespaces(); 
        
        logger.info("Backing-up to file: " + filePath);
        
        initialiseFileOutput(filePath);
        
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
        
        sb = new StringBuilder();
        
        logger.info("Backing Up Namespaces:");
        
        for (String namespace : namespaces) {
            if (isNamespaceBackupPermitted(namespace)) {
                logger.info("SUCCESS:\t" + namespace);
                outputNamespaceCPSProperties(namespace, framework);
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
    private void outputNamespaceCPSProperties(String namespace, IFramework framework) throws FrameworkException {

        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(namespace);
        Map<String, String> properties = cps.getAllProperties();
        
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
     */ 
    private void initialiseFileOutput(String filePath) {
        
        path = Paths.get(filePath);
        try {
            if (!path.toFile().exists()) {
                if (!path.getParent().toFile().exists()) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
            }
        } catch (IOException e) {
            logger.error("Unable to create CPS backup file in location specified: " + path.toUri().toString(), e);
        }
    }
}