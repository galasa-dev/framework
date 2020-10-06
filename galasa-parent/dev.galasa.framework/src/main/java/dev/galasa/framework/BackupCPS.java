/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
    
    private Log  logger  =  LogFactory.getLog(this.getClass());
    
    private final boolean         FILE_APPEND     = false;
    private final boolean         autoFlush         = true;
    
    String charset = "UTF-8";
    
    private File                 file;
    private FileOutputStream     fos;
    private OutputStreamWriter     osw;
    private BufferedWriter         bw;
    private PrintWriter         pw;
    
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
        
        try {
            initialisePrintWriter(filePath);
        } catch (Exception e) {
            throw new FrameworkException("Error Initialising Print Writer", e);
        }
        
        outputCPSProperties(namespaces, framework);
        
        closePrintWriter();
        
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
                outputNamespaceCPSProperties(namespace, framework);
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
            String output = prop.getKey() + "=" + prop.getValue();
            pw.println(output);
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
     * <p>Initialises a PrintWriter object for use in output of CPS properties</p>
     * 
     * @param filePath
     * @return 
     * @throws IOException 
     */ 
    private void initialisePrintWriter(String filePath) throws IOException {
        
        file = new File(filePath);
        
        if(!file.exists()) {
            if(!file.getParentFile().exists()) {
                logger.info("Creating directory(s): " + filePath);
                file.getParentFile().mkdirs();
            }
        }
        
        fos = new FileOutputStream(file, FILE_APPEND);
        osw = new OutputStreamWriter(fos, charset);
        bw = new BufferedWriter(osw);
        pw = new PrintWriter(bw, autoFlush);
    }
    
    /**
     * <p>Closes the PrintWriter object.</p>
     * 
     * @param filePath
     * @return 
     */ 
    private void closePrintWriter() {
        if(pw != null) {
            pw.close();
        } else {
            logger.debug("Attempted to close PrintWriter, but no PrintWriter instance was found.");
        }
    }
}