/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020, 2021.
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
    
    private Map<String, IConfigurationPropertyStoreService>         namespaceCPS = new HashMap<>();
    
    private boolean         DRY_RUN = false;
    
    List<String>            forbiddenNamespaces = new ArrayList<>();
    
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
     * @param dryRun
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
        Properties propsShared = getIntersect(propsFromFile, propsFromCPS);
        
        // Get all properties that need to be updated - and output all properties staying the same.
        Properties propsToUpdate = keepProperties(propsShared, propsFromCPS);
        
        createProperties(propsToCreate);
        updateProperties(propsToUpdate, propsFromCPS);
        deleteProperties(propsToDelete);
        
        logger.info("Finished restoring properties to CPS ");
        
        frameworkInitialisation.shutdownFramework();
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
    
    /**
     * <p>Fetches all properties from the current CPS</p>
     * @return Properties
     * @throws ConfigurationPropertyStoreException
     */
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
    
    /**
     * <p> Fetches all properties from the specified namespace</p>
     * @param namespace
     * @return Properties
     * @throws ConfigurationPropertyStoreException
     */
    private Properties getNamespaceProperties(String namespace) throws ConfigurationPropertyStoreException {
        Properties properties = new Properties();
        
        ensureCPSExists(namespace);
        Map<String, String> nsProperties = namespaceCPS.get(namespace).getAllProperties();
        for(Entry<String, String> prop: nsProperties.entrySet()) {
            properties.put(prop.getKey(), prop.getValue());
        }
        
        return properties;
    }
    
    /**
     * <p>Returns the relative complement of propsA \ propsB</p>
     * <p>(All entries that are in propsA but not probsB)
     * @param propsA
     * @param propsB
     * @return Properties
     */
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
    
    /**
     * <p>Returns the intersection of propsA /\ propsB</p>
     * <p>(All entries that are in both propsA and propsB)</p>
     * @param propsA
     * @param propsB
     * @return
     */
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
    
    /**
     * <p>Creates properties (all those specified within the props param) within the CPS</p>
     * @param props
     * @throws ConfigurationPropertyStoreException
     */
    private void createProperties(Properties props) throws ConfigurationPropertyStoreException {
        
        outputSectionStart("CREATE [KEY AND VALUE]");
        
        // Convert to list and then sort the list alphabetically        
        List<String> propsList = new ArrayList<>(props.stringPropertyNames());
        java.util.Collections.sort(propsList, java.text.Collator.getInstance());
        
        boolean propsCreated = false;
        
        for (String key : propsList) {
            if (!isValidProperty(key)) {
                logger.warn("Invalid Property: " + key);
                continue;
            }
            String namespace = getPropertyPrefix(key);
            String property = getPropertySuffix(key);
            String value = props.getProperty(key);
            
            logger.info(key + " = " + value);
            
            propsCreated = true;
            
            if(!DRY_RUN) { 
                // Create the property
                ensureCPSExists(namespace);
                namespaceCPS.get(namespace).setProperty(property, value);
            }
        }
        
        if (!propsCreated) {
            logger.info("NONE");
        }
        
        outputSectionStop();
    }
    
    /**
     * <p>Returns all the properties that need to be updated in CPS. Additionally outputs to the log those properties which are staying the same.</p>
     * @param newProps
     * @param oldProps
     * @return
     * @throws ConfigurationPropertyStoreException
     */
    private Properties keepProperties(Properties newProps, Properties oldProps) throws ConfigurationPropertyStoreException {
        // Convert to list and then sort the list alphabetically        
        List<String> propsList = new ArrayList<>(newProps.stringPropertyNames());
        java.util.Collections.sort(propsList, java.text.Collator.getInstance());
        
        Properties propsToUpdate = new Properties();
        
        outputSectionStart("NONE [VALUE STAYING THE SAME]");
        
        boolean propsKept = false;

        for (String key : propsList) {
            if (!isValidProperty(key)) {
                logger.warn("Invalid Property: " + key);
                continue;
            }

            if(oldProps.getProperty(key).equals(newProps.getProperty(key))){
                logger.info(key + " = " + oldProps.getProperty(key));
                propsKept = true;
            } else {
                propsToUpdate.put(key, newProps.get(key));
            }
        }
        
        if (!propsKept) {
            logger.info("NONE");
        }
        
        outputSectionStop();
        
        return propsToUpdate;
    }
    
    /**
     * <p>Compares the values of two sets of properties (passed as parameters), updating those that have different values within the CPS.</p>
     * @param newProps
     * @param oldProps
     * @throws ConfigurationPropertyStoreException
     */
    private void updateProperties(Properties newProps, Properties oldProps) throws ConfigurationPropertyStoreException {
        
        // Convert to list and then sort the list alphabetically        
        List<String> propsList = new ArrayList<>(newProps.stringPropertyNames());
        java.util.Collections.sort(propsList, java.text.Collator.getInstance());
        
        outputSectionStart("UPDATE [VALUE]");
        
        boolean propsUpdated = false;
        
        // Update Properties
        for (String key : propsList) {
            String namespace = getPropertyPrefix(key);
            String property = getPropertySuffix(key);
            
            String oldVal = oldProps.getProperty(key);
            String newVal = newProps.getProperty(key);
            
            logger.info(key);
            logger.info("\tOLD VALUE: " + oldVal);
            logger.info("\tNEW VALUE: " + newVal);
            propsUpdated = true;
            
            if(!DRY_RUN) { 
                ensureCPSExists(namespace);
                namespaceCPS.get(namespace).setProperty(property, newVal);
            }
        }
        
        if (!propsUpdated) {
            logger.info("NONE");
        }
        
        outputSectionStop();
        
    }
    
    /**
     * <p>Deletes properties (all those specified within the props param) within the CPS</p>
     * @param props
     * @throws ConfigurationPropertyStoreException
     */
    private void deleteProperties(Properties props) throws ConfigurationPropertyStoreException {
        
        outputSectionStart("DELETE [KEY AND VALUE]");
        
        // Convert to list and then sort the list alphabetically
        List<String> propsList = new ArrayList<>(props.stringPropertyNames());
        java.util.Collections.sort(propsList, java.text.Collator.getInstance());
        
        boolean propsDeleted = false;
        
        for (String key : propsList) {
            if (!isValidProperty(key)) {
                logger.warn("Invalid Property: " + key);
                continue;
            }
            String namespace = getPropertyPrefix(key);
            String property = getPropertySuffix(key);
            String value = props.getProperty(key);
            
            logger.info(key + " = " + value);
            
            propsDeleted = true;
            
            // Delete the property
            
            if(!DRY_RUN) {
                ensureCPSExists(namespace);
                namespaceCPS.get(namespace).deleteProperty(property);
            }
        }
        
        if(!propsDeleted) { 
            logger.info("NONE");
        }
        
        outputSectionStop();
    }
    
    /**
     * <p>Utility to return some text if a dry-run is taking place. Returns an empty string otherwise.</p>
     * @return
     */
    private String getDryRunTitleText() {
        String dryRunText = "DRY RUN";
        
        String output = "";
        
        if (DRY_RUN) {
            output = "[ " + dryRunText + " ]";
        }
        return output;
    }
    
    /**
     * <p>Utility for outputting a section header before a series of properties.</p>
     * @param message
     */
    private void outputSectionStart(String message) {
        logger.info("");
        outputSectionMessage("CPS RESTORATION    " + getDryRunTitleText(), 
                "", "ACTION: " + message, "", ">>> START <<<");
        logger.info("");
    }
    
    /**
     * <p>Utility for outputting a section footer after a series of properties.</p>
     */
    private void outputSectionStop() {
        logger.info("");
        outputSectionMessage(">>> STOP <<<");
        logger.info("");
    }
    
    /**
     * <p>Utility for outputting a section header or footer (a message surrounded by asterisk '*').<p>
     * @param messages
     */
    private void outputSectionMessage(String... messages) {
        String bannerAsterisk = "*******************************************************************";
        logger.info(bannerAsterisk);
        
        for(String message : messages) {
            logger.info(String.format("*  %-62s *", message));
        }
        
        logger.info(bannerAsterisk);
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
        
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9\\-\\_]+\\.){2,}[a-zA-Z0-9\\-\\_]+$");
        Matcher matcher = pattern.matcher(key);
        return matcher.find();
    }
    
}
