/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.framework;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    
    private Map<String, IConfigurationPropertyStoreService>     namespaceCPS 
                            = new HashMap<String ,IConfigurationPropertyStoreService>();
    
    List<String> forbiddenNamespaces = new ArrayList<String>();
    
    public RestoreCPS(){
    	forbiddenNamespaces.add("dss");
        forbiddenNamespaces.add("certificate");
        forbiddenNamespaces.add("secure");
    }
    
    /**
     * <p>Restores configuration properties from the specified file to the Configuration Property Store (CPS)</p>
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @param filePath
     * @return
     * @throws FrameworkException
     * @throws IOException 
     */
    public void restore(Properties bootstrapProperties, Properties overrideProperties, String filePath) throws FrameworkException, IOException {
        logger.info("Initialising CPS Restore Service");
        
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }
        
        framework = frameworkInitialisation.getFramework();
        
        Properties properties = getProperties(filePath);
        if (!properties.isEmpty()) {
            restoreProperties(properties);
        } else {
            throw new FrameworkException("Cannot restore properties. The specified file is either empty or was not found.");
        }
    }
    
    /**
     * <p>Fetches configuration properties from a specified file</p>
     * 
     * @param filePath
     * @return properties
     * @throws IOException 
     */
    private Properties getProperties(String filePath) throws IOException {
        
        Properties properties = new Properties();

        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            properties.load(inputStream);
        } catch (Exception e) {
        	throw new IOException("Couldn't load properties from specified file: ".concat(filePath), e);
        }
        
        return properties;

    }
    
    /**
     * <p>Iterates through the properties in the supplied object, restoring them one-by-one to the Configuration Property Store<p>
     * 
     * @param properties (instance of object: Properties)
     * @return 
     */
    private void restoreProperties(Properties properties) throws ConfigurationPropertyStoreException {
        
        for (Entry<Object, Object> prop : properties.entrySet()) {
            if (isValidProperty(prop.getKey().toString())) {
                restoreProperty(prop.getKey().toString(), prop.getValue().toString());
            } else {
                logPropertyRestore("invalid", "n/a", "n/a", "n/a", "n/a");
            }
        }
                
    }
    
    /**
     * <p>Restores individual property to CPS</p>
     * 
     * @param key (String)
     * @param value (String)
     * @return
     * @throws ConfigurationPropertyStoreException
     */
    private void restoreProperty(String key, String newValue) throws ConfigurationPropertyStoreException {
        
        String namespace = getPropertyPrefix(key);
        String property = getPropertySuffix(key);
        
        if (!forbiddenNamespaces.contains(namespace)){
            
            ensureCPSExists(namespace);
            
            String currentValue = getExistingValue(namespace, property);
            
            namespaceCPS.get(namespace).setProperty(property, newValue);

            logPropertyRestore(getStatusCRU(newValue, currentValue), namespace, property, newValue, currentValue);

        } else {
            logPropertyRestore("denied", namespace, property, "n/a", "n/a");
        }
        
    }

    /**
     * <p>Initialise an instance of IConfigurationPropertyStoreService for the specified namespace if one doesn't already exist.</p>
     * @param namespace
     * @throws ConfigurationPropertyStoreException
     */
    private void ensureCPSExists(String namespace) throws ConfigurationPropertyStoreException {
        if (!namespaceCPS.containsKey(namespace)){
            namespaceCPS.put(namespace, framework.getConfigurationPropertyService(namespace));
        }
    }

    /**
     * <p>Returns the status message to be displayed based on the new and current value of the property that was set<p>
     * @param newValue
     * @param currentValue
     * @return
     */
    private String getStatusCRU(String newValue, String currentValue) {
        if (currentValue != null) {
            return getValueChangeStatus(newValue, currentValue);
        } else {
            return "created";
        }
    }

    /**
     * <p>Returns status message to identify whether a property's value was updated or remained the same<p>
     * @param newValue
     * @param currentValue
     * @return
     */
    private String getValueChangeStatus(String newValue, String currentValue) {
        if (currentValue.equals(newValue)) {
            return "no_change";
        } else {
            return "updated";
        }
    }
    
    /**
     * <p>Retrieves Property Prefix (after first dot ".")</p>
     * 
     * @param propertyName
     * @return
     */
    private String getPropertyPrefix(String propertyName) throws ConfigurationPropertyStoreException {
        return propSplit(propertyName, 0);
    }
    
    /**
     * <p>Retrieves Property Suffix (after first dot ".")</p>
     * 
     * @param propertyName
     * @return
     */
    private String getPropertySuffix(String propertyName) throws ConfigurationPropertyStoreException {
        return propSplit(propertyName, 1);
    }
    
    /**
     * <p>Splits a string into two parts: a prefix and a suffix.</p>
     * <p>Prefix: Anything before the first dot "."</p>
     * <p>Suffix: Anything after the first dot "."</p>
     * 
     * <p>Position specified as 0 or 1</p>
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
        /**
         *  Regex matches (at least) three words (of one letter or more) separated by dots (".")
         *  e.g. framework.foo.bar or framework.foo.bar.fizz.buzz
         */
        
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9]+\\.){2,}[a-zA-Z0-9]+$");
        Matcher matcher = pattern.matcher(key);
        boolean matchFound = matcher.find();
        return matchFound;
    }
       
    /**
     * <p>Retrieves the current/existing value for a specified property.</p>
     * <p>This wrapper is required due to properties only being retrievable using both a prefix and a suffix.</p>
     * 
     * @param namespace
     * @param property
     * @return existingValue
     * @throws ConfigurationPropertyStoreException 
     */
    private String getExistingValue(String namespace, String property) throws ConfigurationPropertyStoreException {
     
        String existingValue = 
                namespaceCPS.get(namespace).getProperty(getPropertyPrefix(property), getPropertySuffix(property));
                
        return existingValue;
        
    }

    /**
     * <p>Log property restore status</p>
     * @param status
     * @param namespace
     * @param property
     * @param currentValue
     * @param newValue
     */
    private void logPropertyRestore(String status, String namespace, String property, String currentValue, String newValue){
        logger.info(String.format("STATUS: %-10s\tNAMESPACE: %s\tPROPERTY: %s\tOLDVALUE: %s\tNEWVALUE: %s", status, namespace, property, currentValue, newValue));
    }

}
