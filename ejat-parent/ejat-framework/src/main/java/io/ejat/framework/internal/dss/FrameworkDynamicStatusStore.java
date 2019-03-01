package io.ejat.framework.internal.dss;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IDynamicResource;
import io.ejat.framework.spi.FrameworkDynamicResource;
import io.ejat.framework.spi.IDynamicRun;
import io.ejat.framework.spi.FrameworkDynamicRun;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.DynamicStatusStoreException;

/**
 * <p>Temp</p>
 * 
 * @author Bruce Abbott
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
	 * @param key
	 * @return
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
	 * @return
	 * @throws DynamicStatusStoreException
	 */
    public IDynamicRun getDynamicRun() throws DynamicStatusStoreException {
        FrameworkDynamicRun tempDynamicRun = new FrameworkDynamicRun();
        return tempDynamicRun;
    }

    public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {

    }

    public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {

    }

    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue) throws DynamicStatusStoreException{
        return true;
    }

    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue, @NotNull Map<String, String> others) throws DynamicStatusStoreException {
        return true;
    }

    public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
        return "temp";
    }
    
    public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        Map<String, String> tempMap = new HashMap<>();
        return tempMap;
    }

    public void delete(@NotNull String key) throws DynamicStatusStoreException {

    }

    public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {

    }

    public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {

    }
}