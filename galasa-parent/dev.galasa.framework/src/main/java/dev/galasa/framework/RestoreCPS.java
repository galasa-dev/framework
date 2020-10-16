/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

@Component(service = { RestoreCPS.class })
public class RestoreCPS {
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    private IFramework      framework;
    
    private Path            path;
    
    /**
     * <p>Restores CPS properties from a specified file</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @param filePath
     * @return
     * @throws FrameworkException
     */
    public void restore(Properties bootstrapProperties, Properties overrideProperties, String filePath) throws FrameworkException {
        logger.info("Initialising CPS Backup Service");
        
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }
        
        framework = frameworkInitialisation.getFramework();
        
        List<String> lines = getLinesFromFile(filePath);
        
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Error Creating InputStream Using File: " + path.toUri().toString(), e);
        }
        
        Properties propTest = new Properties();
        
        if (inputStream != null) {
            try {
                propTest.load(inputStream);
            } catch (IOException e) {
                logger.error("Error Loading Properties From InputStream", e);
            }
        }
        
        Map<String, IConfigurationPropertyStoreService> namespaceCPS = new HashMap<String ,IConfigurationPropertyStoreService>();
        
        for (Entry<Object, Object> prop : propTest.entrySet()) {
            String key = prop.getKey().toString();
            String value = prop.getValue().toString();
            String[] kvp = key.split("\\.", 2);
            String namespace = kvp[0];
            String property = kvp[1];
            
            logger.debug("Namespace: " + namespace + " Property: " + property);
            if (!namespaceCPS.containsKey(namespace)) {
                logger.debug("Creating CPS from Namespace: " + namespace);
                namespaceCPS.put(namespace, framework.getConfigurationPropertyService(namespace));
            }
            namespaceCPS.get(namespace).setProperty(property, value);
            
        }
        

    }
    
    public List<String> getLinesFromFile(String filePath){
        List<String> lines = new ArrayList<String>();
        path = Paths.get(filePath);
        try {
            if (!path.toFile().exists()) {
                lines = Files.readAllLines(path);
            }
            
        } catch (IOException e) {
            logger.error("Unable to create CPS backup file in location specified: " + path.toUri().toString(), e);
        }
        
        return lines;
    }
}