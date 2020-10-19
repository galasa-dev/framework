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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

@Component(service = { RestoreCPS.class })
public class RestoreCPS {
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    private IFramework      framework;
    
    private Path            path;
    
    private Map<String, IConfigurationPropertyStoreService>     namespaceCPS 
                            = new HashMap<String ,IConfigurationPropertyStoreService>();
    
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
        logger.info("Initialising CPS Restore Service");
        
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }
        
        framework = frameworkInitialisation.getFramework();
        
        Properties properties = getProperties(filePath);
        if (properties != null) {
            restoreProperties(properties);
        } else {
            logger.info("No properties found to restore.");
        }
    }
    
    private Properties getProperties(String filePath) {
        
        Properties properties = new Properties();
        
        path = Paths.get(filePath);
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Error Creating InputStream Using File: " + path.toUri().toString(), e);
        }
        
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                logger.error("Error Loading Properties From InputStream", e);
            }
        }
        
        return properties;

    }
    
    private void restoreProperties(Properties properties) throws ConfigurationPropertyStoreException {
        
        logger.info(String.format("| %-10s | %-15s | %-30s | %-15s | %-15s |", "STATUS", "NAMESPACE", "PROPERTY", "OLD VALUE", "NEW VALUE"));
        logger.info(String.format("|%-12s|%-17s|%-32s|%-17s|%-17s|", getDashes(12), getDashes(17), getDashes(32), getDashes(17), getDashes(17)));
        
        Map<String, String> invalidProperties = new HashMap<String, String>();
        
        for (Entry<Object, Object> prop : properties.entrySet()) {
            if (isValidProperty(prop.getKey().toString())) {
                restoreProperty(prop);
            } else {
                invalidProperties.put(prop.getKey().toString(), prop.getValue().toString());
            }
        }
        logger.info(String.format("|%-12s|%-17s|%-32s|%-17s|%-17s|", getDashes(12), getDashes(17), getDashes(32), getDashes(17), getDashes(17)));

        if (invalidProperties.size() > 0) {
            logger.info("Could not restore the following due to malformed property name:");
            for (Entry<String, String> prop : invalidProperties.entrySet()) {
                logger.info("NAME: " + prop.getKey().toString() + "\tVALUE: " + prop.getValue().toString());
            }
        }
        
    }
    
    
    /**
     * <p>Splits a string into two parts: a prefix and a suffix.</p>
     * <p>Prefix: Anything before the first dot "."</p>
     * <p>Suffix: Anything after the first dot "."</p>
     * 
     * <p>Position specified as 0 or 1
     * 
     * @param str
     * @param position
     * @return dashes
     */
    private void restoreProperty(Entry<Object, Object> prop) throws ConfigurationPropertyStoreException {
        
        String namespace = getPropertyPrefix(prop.getKey().toString());
        String property = getPropertySuffix(prop.getKey().toString());
        
        String newValue = prop.getValue().toString();
        String currentValue = "";
        
        String propertyRestoreStatus = "";
        
        if (!namespaceCPS.containsKey(namespace)) {
            // Create CPS instance if it doesn't yet exist
            namespaceCPS.put(namespace, framework.getConfigurationPropertyService(namespace));
        }
        
        currentValue = getExistingValue(namespace, property);
        namespaceCPS.get(namespace).setProperty(property, newValue);
        
        if (currentValue != null) {
            if (currentValue.equals(newValue)) {
                propertyRestoreStatus = "NO CHANGE";
            } else {
                propertyRestoreStatus = "UPDATED";
            }
        } else {
            propertyRestoreStatus = "CREATED";
        }
        
        logger.info(String.format("| %-10s | %-15s | %-30s | %-15s | %-15s |", propertyRestoreStatus, namespace, property, currentValue, newValue));
        
    }
    
    /**
     * <p><Retrieves Property Prefix</p>
     * 
     * @param propertyName
     * @return dashes
     */
    private String getPropertyPrefix(String propertyName) throws ConfigurationPropertyStoreException {
        return propSplit(propertyName, 0);
    }
    
    
    private String getPropertySuffix(String propertyName) throws ConfigurationPropertyStoreException {
        return propSplit(propertyName, 1);
    }
    
    /**
     * <p>Splits a string into two parts: a prefix and a suffix.</p>
     * <p>Prefix: Anything before the first dot "."</p>
     * <p>Suffix: Anything after the first dot "."</p>
     * 
     * <p>Position specified as 0 or 1
     * 
     * @param str
     * @param position
     * @return dashes
     */
    private String propSplit(String str, int position) throws ConfigurationPropertyStoreException {
        
        String[] kvp = str.split("\\.", 2);
        if (kvp.length <= 1) {
            throw new ConfigurationPropertyStoreException("Invalid Property Format: " + str);
        }
        return kvp[position];
    }
    
    /**
     * <p>Checks for property validity (whether there is a prefix and a suffix, separated by a dot ".").</p>
     * 
     * @param key
     * @return boolean
     */
    private boolean isValidProperty(String key) {
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9]+\\.){2,}[a-zA-Z0-9]+$");
        Matcher matcher = pattern.matcher(key);
        boolean matchFound = matcher.find();
        return matchFound;
    }
    
    /**
     * <p>Utility to fetch a string with a specified number of dashes.</p>
     * <p>Useful for table output in console.</p>
     * 
     * @param num
     * @return dashes
     */
    private String getDashes(int num) {
        String dashes = new String(new char [num]).replace("\0", "-");
        return dashes;
    }
    
    
    /**
     * <p>Retrieves the current/existing value for a specified property.</p>
     * <p>This wrapper is required due to properties only being retrievable using both a prefix and a suffix.</p>
     * 
     * @param namespace
     * @param key
     * @return existingValue
     * @throws ConfigurationPropertyStoreException 
     */
    private String getExistingValue(String namespace, String key) throws ConfigurationPropertyStoreException {
     
        String existingValue = 
                namespaceCPS.get(namespace).getProperty(getPropertyPrefix(key), getPropertySuffix(key));
                
        return existingValue;
        
    }
}
