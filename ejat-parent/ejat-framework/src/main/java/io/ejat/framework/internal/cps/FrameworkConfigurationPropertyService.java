package io.ejat.framework.internal.cps;

import java.util.Properties;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStore;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IFramework;

/**
 * p>This class is used to drive the registered CPS service, and retireve values from the CPS Store, impletmenting the IConfiguration interface.</p>
 * 
 * @author James Davies
 */
public class FrameworkConfigurationPropertyService implements IConfigurationPropertyStoreService{
    private String namespace;
    private Properties record;
    private Properties overrides;
    private IConfigurationPropertyStore cpsStore;

    /**
     * <p>This constructor ensures that the registered CPS service (fpf or etcd3) is known, as wells as accepting the overrides and record properties. Namespace of the manager which 
     * is using this class is also registered at this point.</p>
     * 
     * @author James Davies
     * 
     * @param framework - not currently used.
     * @param cpsService - the registered service for the CPS
     * @param overrides - property values to be selected as preference from these properties
     * @param record - a properties object for recording the keys accessed and where from <Location>=<KeyAccessed>
     * @param namespace - The name space for keys for a specfic manager
     */
    public FrameworkConfigurationPropertyService(IFramework framework, IConfigurationPropertyStore cpsStore, Properties overrides, Properties record, String namespace) {
        this.namespace = namespace;
        this.record = record;
        this.overrides = overrides;
        this.cpsStore = cpsStore;
    }

    /**
     * <p>This method returns a String value for a key requested. This looks for any key that has a non null value through the hierachical structure 
     *  of the keys in the CPS:
     *  zos.image.PLEXMA.MVMA.credentialid
     *  zos.image.PLEXMA.credentialid
     *  zos.image.credentialid
     *  With the infixes of PLEXMA and MVMA being preffered. List in order of checking.
     *  All values checked are also checked in the overrides properties, which as expceted should override any value in the CPS.</p>
     * 
     *  @param prefix - in the above example is "image"
     *  @param suffix - in the above example is "credentialid"
     *  @param infixes - the hierachal structure above, supplied in order, e.g "PLEXMA", "MVMA"
     *  @return - string value for the key requested
     *  @throws ConfigurationPropertyStoreException - throws the caught exception from the getValueAndMakeAccessRecord() method.
     */
    public String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes) throws ConfigurationPropertyStoreException {
        if(infixes==null) {
            infixes = new String[0];
        }
        String[] keys = createOrderedKeyList(prefix, suffix, infixes);
        String value = null;
        for (String key: keys){
            value = getValueAndMakeAccessRecord(key);
            if (value!=null){
                return value;
            }
        }
        return null;
    }
    /**
     * <p>This method retrievs all the keys that would be serached in the order that they would be searched. For example:
     *  zos.image.PLEXMA.MVMA.credentialid
     *  zos.image.PLEXMA.credentialid
     *  zos.image.credentialid</p>
     * 
     * @param prefix - in the above example is "image"
     *  @param suffix - in the above example is "credentialid"
     *  @param infixes - the hierachal structure above, supplied in order, e.g "PLEXMA", "MVMA"
     *  @return - an array of the keys as strings in the order they would be serached
     */
    public String[] reportPropertyVariants(@NotNull String prefix, @NotNull String suffix, String... infixes) {
        if(infixes==null) {
            infixes = new String[0];
        }
        return createOrderedKeyList(prefix, suffix, infixes);
    }

    /**
     * <p>This method retrievs all the keys that would be serached in the order that they would be searched. For example:
     *  zos.image.PLEXMA.MVMA.credentialid
     *  zos.image.PLEXMA.credentialid
     *  zos.image.credentialid</p>
     * 
     * @param prefix - in the above example is "image"
     *  @param suffix - in the above example is "credentialid"
     *  @param infixes - the hierachal structure above, supplied in order, e.g "PLEXMA", "MVMA"
     *  @return - comma separated property names in the order they would be serached
     */
    public String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes) {
    	String[] variants = reportPropertyVariants(prefix, suffix, infixes);
    	
    	StringBuilder sb = new StringBuilder();
    	for(String variant : variants) {
    		if (sb.length() == 0) {
    			sb.append("[");
    		} else {
    			sb.append(",");
    		}
    		sb.append(variant);
    	}
    	sb.append("]");
    	return sb.toString();
    }

    /**
     * <p>This private method is used by the getProperty() method to return a String value for a given key and record any key/value accessed.
     * This method insures it does the check of the overrides properties first before checking any cpsService.</p>
     * 
     * @param key - generated from the prefix, suffix and infixes provided to the getProprty().
     * @return - String value returned from either CPS store or the overrides.
     * @throws ConfigurationPropertyStoreException - throws an exception is the registered CPS service is not available.
     */
    private String getValueAndMakeAccessRecord(String key) throws ConfigurationPropertyStoreException{
        String value;

        value = overrides.getProperty(key);
        if (value!=null) {
            record.put(key, value);
            return value;
        }
        value = cpsStore.getProperty(key);

        if (value != null){
            record.put(key, value);
            return value;
        }
        record.put(key, "*** MISSING ***");
        return null;
    }

    /**
     * <p>This method creates an array filled in order of all the keys to try and find in both the overrides properties or the CPS store.
     *  The order of this keyList is important as it will be used by the getProprty() method.</p>
     * 
     * @param prefix - in the above example is "image"
     * @param suffix - in the above example is "credentialid"
     * @param infixes - the hierachal structure above, supplied in order, e.g "PLEXMA", "MVMA"
     * @return - String array of the keys in the correct order to check from 0-x
     */
    private String[] createOrderedKeyList(String prefix, String suffix, String... infixes) {
        String[] keys = new String[(infixes.length+1)];
        String [] infixOrder = getInfixOrder(infixes);
        
        for (int i=0;i<infixOrder.length;i++) {
            keys[i] = namespace + "." + prefix + infixOrder[i] + suffix;
        }

        keys[infixes.length] = namespace + "." + prefix +"."+ suffix;

        return keys;
    }

    /**
     * <p>This method takes passed infixes and generates the list of infixes of hierachy order, which is will be surrounded by the prefix and suffix to 
     *  genterate the kys to be searched.</p>
     * 
     * @param infixes - all the heirachy levels that could be appended to the namespace and prefix.
     * @return - string array of the infixes that need to be checked.
     */
    private String[] getInfixOrder(String... infixes) {
        String[] infixOrderList = new String[infixes.length];
        for (int i=0; i<infixes.length; i++) {
            StringBuilder majorInfix = new StringBuilder();
            for (int j=0; j<(infixes.length-i);j++) {
                majorInfix.append("." + infixes[j]);
            }
            majorInfix.append(".");
            infixOrderList[i] = majorInfix.toString();
        }
        return infixOrderList;
    }
}