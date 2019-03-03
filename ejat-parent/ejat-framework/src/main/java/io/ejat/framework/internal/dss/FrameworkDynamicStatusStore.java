package io.ejat.framework.internal.dss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.FrameworkDynamicResource;
import io.ejat.framework.spi.FrameworkDynamicRun;
import io.ejat.framework.spi.IDynamicResource;
import io.ejat.framework.spi.IDynamicRun;
import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;

/**
 * <p>A DSS Stub which is passed to the managers that will pass the requests to the activated DSS Service</p>
 * 
 * @author Bruce Abbott
 * 
 */
public class FrameworkDynamicStatusStore implements IDynamicStatusStore {
    private IDynamicStatusStoreService dssService;
    private String namespace;

    /**
     * <p>Temp</p>
     * 
     * @param framework - not currently used.
     * @param dssService - the registered service for the DSS
     * @param namespace - The namespace for keys for a specfic manager
     */
    public FrameworkDynamicStatusStore(IFramework framework, IDynamicStatusStoreService dssService, String namespace) {
        Objects.requireNonNull(dssService);
        Objects.requireNonNull(namespace);
        
        this.dssService = dssService;
        this.namespace = namespace;
    }

    /**
	 * <p>Retrieve interface to control a dynamic resource represented in 
	 * the framework area. This is to allow the resource being managed to be automatically
	 * represented on the Web UI and the Eclipse Automation Views.</p>
	 * 
	 * <p>The properties the framework create from will be dss.framework.resource.namespace.resourceKey .  
	 * After that the manager can set the property names as necessary.</p>
	 * 
	 * <p>For example,  if the zOS Security Manager is controlling a set of userids on cluster PlexMA,
	 *  the namespace is already set to 'zossec', the property key would be 'PLEXMA.userid.JAT234'.  This would 
	 *  result in the property 'dss.framework.resource.zossec.PLEXMA.userid.JAT234=L3456'.  The automation views would 
	 *  build a tree view of the properties starting 'dss.framework.resource'</p> 
	 * 
	 * @param key - The resource key to prefix the keys along with the namespace
	 * @return A tailored IDynamicResource
	 * @throws DynamicStatusStoreException
	 */
    public IDynamicResource getDynamicResource(String resourceKey) throws DynamicStatusStoreException {
        FrameworkDynamicResource tempDynamicResource = new FrameworkDynamicResource();
        return tempDynamicResource;
    }

    /**
	 * <p> Retrieve an interface to update the Run status with manager related information.  This is information
	 * above what the framework would display, like status,  no. of methods etc.</p>
	 * 
	 * <p>One possible use would be the zOS Manager reporting the primary zOS Image the test is running on.</p>
	 * 
	 * @return The dynamic run resource tailored to this namespaces
	 * @throws DynamicStatusStoreException
	 */
    public IDynamicRun getDynamicRun() throws DynamicStatusStoreException {
        FrameworkDynamicRun tempDynamicRun = new FrameworkDynamicRun();
        return tempDynamicRun;
    }

    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#put(java.lang.String, java.lang.String)
     */
    public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
        dssService.put(prefixKey(key), value);
    }

    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#put(java.util.Map)
     */
    public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
        Objects.requireNonNull(keyValues);

        //*** Copy all the keys and prefix them
        HashMap<String, String> newKeyValues = new HashMap<>();
        for(Entry<String, String> entry : keyValues.entrySet()) {
            String oKey = entry.getKey();
            String oValue = entry.getValue();
            
            Objects.requireNonNull(oKey);
            Objects.requireNonNull(oValue);
            
            newKeyValues.put(prefixKey(oKey), oValue);
        }
        
        dssService.put(newKeyValues);
    }

    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#putSwap(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue) throws DynamicStatusStoreException{
        Objects.requireNonNull(newValue);
        return dssService.putSwap(prefixKey(key), oldValue, newValue);
    }

    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#putSwap(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue, @NotNull Map<String, String> others) throws DynamicStatusStoreException {
        Objects.requireNonNull(newValue);
        Objects.requireNonNull(others);

        //*** Copy all the other keys and prefix them
        HashMap<String, String> newOthers = new HashMap<>();
        for(Entry<String, String> entry : others.entrySet()) {
            String oKey = entry.getKey();
            String oValue = entry.getValue();
            
            Objects.requireNonNull(oKey);
            Objects.requireNonNull(oValue);
            
            newOthers.put(prefixKey(oKey), oValue);
        }
        
        return dssService.putSwap(prefixKey(key), oldValue, newValue, newOthers);
    }

    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#get(java.lang.String)
     */
    public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
        return dssService.get(prefixKey(key));
    }
    
    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#getPrefix(java.lang.String)
     */
    public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        return dssService.getPrefix(prefixKey(keyPrefix));
    }

    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#delete(java.lang.String)
     */
    public void delete(@NotNull String key) throws DynamicStatusStoreException {
        dssService.delete(prefixKey(key));
    }

    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#delete(java.util.Set)
     */
    public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
        Objects.requireNonNull(keys);

        //*** Copy all the keys and prefix them
        HashSet<String> newKeys = new HashSet<>();
        for(String key : newKeys) {
            Objects.requireNonNull(key);
            newKeys.add(prefixKey(key));
        }
        
        dssService.delete(newKeys);
   }

    /* (non-Javadoc)
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#deletePrefix(java.lang.String)
     */
    public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        dssService.deletePrefix(prefixKey(keyPrefix));
    }
    
    /**
     * Prefix the supplied key with the namespace
     * 
     * @param key
     * @return
     */
    private String prefixKey(String key) {
        Objects.requireNonNull(key);
        return this.namespace + "." + key;
    }
}