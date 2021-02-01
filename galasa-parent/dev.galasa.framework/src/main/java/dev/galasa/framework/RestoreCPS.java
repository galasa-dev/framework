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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.Property;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

@Component(service = { RestoreCPS.class })
public class RestoreCPS {
    
    private Log             logger  =  LogFactory.getLog(this.getClass());
    
    private IFramework      framework;
    
    private Map<String, IConfigurationPropertyStoreService>     namespaceCPS = new HashMap<>();
    
    private boolean DRY_RUN = false;
    
    List<String> forbiddenNamespaces = new ArrayList<>();
    
    /**
     * Constructor - No params
     */
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
    public void restore(Properties bootstrapProperties, Properties overrideProperties, String filePath, boolean dryRun) throws FrameworkException, IOException {
        logger.info("Initialising CPS Restore Service");
        
        // Initialise Framework
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Service", e);
        }
        
        framework = frameworkInitialisation.getFramework();
        
        // Ensure we know the run characteristic(s)
        DRY_RUN = dryRun;
        
        // Fetch all properties from file
        Properties propsFromFile = getPropertiesFromFile(filePath);
        
        if (propsFromFile.isEmpty()) {
          frameworkInitialisation.shutdownFramework();
          throw new FrameworkException("Cannot restore properties. The specified file is either empty or was not found.");
        }
        
        // Fetch all properties from current cps
        Properties propsFromCPS = getPropertiesFromCPS();
        
        // Get all properties in current CPS store but not in restoration file
        Properties propsToDelete = getComplement(propsFromCPS, propsFromFile);
        
        // Get all properties in restoration file but not in current CPS store
        Properties propsToCreate = getComplement(propsFromFile, propsFromCPS);
        
        // Get all properties in both restoration and current properties
        Properties propsToUpdate = getIntersect(propsFromFile, propsFromCPS);
        
        createProperties(propsToCreate);
        updateProperties(propsToUpdate);
        deleteProperties(propsToDelete);
        
        logger.info("Finished restoring properties to CPS ");
        
        frameworkInitialisation.shutdownFramework();
    }
    
    private void outputSectionStart(String message) {
        logger.info("");
        outputSectionMessage("CPS RESTORATION    " + getDryRunTitleText(), 
                "", message, "", ">>> START <<<");
        logger.info("");
    }
    
    private void outputSectionStop() {
        logger.info("");
        outputSectionMessage(">>> STOP <<<");
        logger.info("");
    }
    
    private void outputSectionMessage(String... messages) {
        String bannerStars = "********************************************************************";
        logger.info(bannerStars);
        
        for(String message : messages) {
            logger.info(String.format("*  %-62s *", message));
        }
        
        logger.info(bannerStars);
    }
    
    /**
     * <p>Fetches configuration properties from a specified file</p>
     * 
     * @param filePath
     * @return properties
     * @throws IOException 
     */
    private Properties getPropertiesFromFile(String filePath) throws IOException {
        
        Properties properties = new Properties();

        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            properties.load(inputStream);
        } catch (Exception e) {
        	throw new IOException("Couldn't load properties from specified file: ".concat(filePath), e);
        }
        
        return properties;
    }
    
    private Properties getPropertiesFromCPS() throws ConfigurationPropertyStoreException {
        Properties properties = new Properties();
        
        // Retrieve all namespaces
        List<String> namespaces = framework.getConfigurationPropertyService("framework").getCPSNamespaces();
        
        // Retrieve properties from those namespaces
        for (String namespace : namespaces) {
            if(!forbiddenNamespaces.contains(namespace)) {
                properties.putAll(getNamespaceProperties(namespace));
            }
        }
        
        return properties;
    }
    
    private Properties getNamespaceProperties(String namespace) throws ConfigurationPropertyStoreException {
        Properties properties = new Properties();
        
        ensureCPSExists(namespace);
        Map<String, String> nsProperties = namespaceCPS.get(namespace).getAllProperties();
        for(Entry<String, String> prop: nsProperties.entrySet()) {
            properties.put(prop.getKey(), prop.getValue());
        }
        
        return properties;
    }
    
    private Properties getComplement(Properties propsA, Properties propsB){
        // Get relative complement of propsA \ propsB
        Properties properties = new Properties();

        Set<Object> propsIntersect = new HashSet<>();
        
        propsIntersect.addAll(propsA.keySet());
        propsIntersect.removeAll(propsB.keySet());
        
        for (Object key : propsIntersect) {
            properties.put(key, propsA.getProperty(key.toString()));
        }
        
        return properties;
    }
    
    private Properties getIntersect(Properties propsA, Properties propsB){
        // Get intesection of propsA /\ propsB
        Properties properties = new Properties();

        Set<Object> propsIntersect = new HashSet<>();
        
        propsIntersect.addAll(propsA.keySet());
        propsIntersect.retainAll(propsB.keySet());

        for (Object key : propsIntersect) {
            properties.put(key, propsA.getProperty(key.toString()));
        }
        
        return properties;
    }
    
    private void createProperties(Properties props) throws ConfigurationPropertyStoreException {
        
        outputSectionStart("CREATING PROPERTIES");
        
        // Convert to list and then sort the list alphabetically        
        List<String> propsList = new ArrayList<>(props.stringPropertyNames());
        java.util.Collections.sort(propsList, java.text.Collator.getInstance());
        
        for (String key : propsList) {
            if (!isValidProperty(key)) {
                logger.warn("Invalid Property: " + key);
                continue;
            }
            String namespace = getPropertyPrefix(key);
            String property = getPropertySuffix(key);
            String value = props.getProperty(key);
            
            ensureCPSExists(namespace);
            
            logger.info(property + " = " + value);
            
            // Create the property
            
            if(!DRY_RUN) { 
                 namespaceCPS.get(namespace).setProperty(property, value);
            }
        }
        
        outputSectionStop();
    }
    
    private void updateProperties(Properties props) throws ConfigurationPropertyStoreException {
        
        // Convert to list and then sort the list alphabetically        
        List<String> propsList = new ArrayList<>(props.stringPropertyNames());
        java.util.Collections.sort(propsList, java.text.Collator.getInstance());
        
        List<String> propsNotUpdated = new ArrayList<>();
        
        outputSectionStart("UPDATING PROPERTIES");
        
        for (String key : propsList) {
            if (!isValidProperty(key)) {
                logger.warn("Invalid Property: " + key);
                continue;
            }
            String namespace = getPropertyPrefix(key);
            String property = getPropertySuffix(key);
            String value = props.getProperty(key);
            
            ensureCPSExists(namespace);
            
            String oldValue = namespaceCPS.get(namespace).getProperty(getPropertyPrefix(property), getPropertySuffix(property));
            
            if(oldValue.equals(value)){
                propsNotUpdated.add(key);
            } else {
                logger.info(key);
                logger.info("\tOLD VALUE: " + oldValue);
                logger.info("\tNEW VALUE: " + value);
                
                if(!DRY_RUN) { 
                    namespaceCPS.get(namespace).setProperty(property, value);
                }
            }
            
            
        }

        outputSectionStop();
        
        
        outputSectionStart("KEEPING PROPERTIES");
        
        for (String key : propsNotUpdated) {
            logger.info(key + " = " + props.getProperty(key));
        }
        
        outputSectionStop();
        
    }

    private void deleteProperties(Properties props) throws ConfigurationPropertyStoreException {
        
        outputSectionStart("DELETING PROPERTIES");
        
        // Convert to list and then sort the list alphabetically
        List<String> propsList = new ArrayList<>(props.stringPropertyNames());
        java.util.Collections.sort(propsList, java.text.Collator.getInstance());
        
        for (String key : propsList) {
            if (!isValidProperty(key)) {
                logger.warn("Invalid Property: " + key);
                continue;
            }
            String namespace = getPropertyPrefix(key);
            String property = getPropertySuffix(key);
            String value = props.getProperty(key);
            
            ensureCPSExists(namespace);
            
            logger.info(property + " = " + value);
            
            // Delete the property
            
            if(!DRY_RUN) {
                 // namespaceCPS.get(namespace).setProperty(property, value);
            }
        }
        
        outputSectionStop();
    }
    
    private String getDryRunTitleText() {
        String dryRunText = "DRY RUN";
        
        String output = "";
        
        if (DRY_RUN) {
            output = "[ " + dryRunText + " ]";
        }
        return output;
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
     * <p>Retrieves Property Prefix (after first dot ".")</p>
     * 
     * @param propertyName
     * @return
     */
    private String getPropertyPrefix(String propertyName) {
        return propSplit(propertyName, 0);
    }
    
    /**
     * <p>Retrieves Property Suffix (after first dot ".")</p>
     * 
     * @param propertyName
     * @return
     */
    private String getPropertySuffix(String propertyName) {
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
    private String propSplit(String str, int position) {
        
        String[] kvp = str.split("\\.", 2);

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
        return matcher.find();
    }
    
}
