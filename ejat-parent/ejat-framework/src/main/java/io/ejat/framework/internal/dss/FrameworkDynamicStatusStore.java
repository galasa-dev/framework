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
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.IDynamicResource;
import io.ejat.framework.spi.IDynamicRun;
import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;


/**
 *  <p>This class is used when the FPF class is being operated as the Key-Value store for the Dynamic Status Store. 
 *  This class registers the Dynamic Status Store as the only DSS.</p>
 * 
 * @author Bruce Abbott
 */

public class FrameworkDynamicStatusStore implements IDynamicStatusStoreService {
	private FrameworkPropertyFile fpf;

	private final IDynamicStatusStore 		 dssStore;
    private final String                     namespace;
	private final String                     prefix;
	
	public FrameworkDynamicStatusStore(IFramework framework, IDynamicStatusStore dssStore, String namespace) {
        Objects.requireNonNull(dssStore);
        Objects.requireNonNull(namespace);

        this.dssStore = dssStore;
        this.namespace = namespace;
        this.prefix = "dss." + this.namespace + ".";
    }

	/*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#put(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
        this.dssStore.put(prefixKey(key), value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#put(java.util.Map)
     */
    @Override
    public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
        Objects.requireNonNull(keyValues);

        // *** Copy all the keys and prefix them
        final HashMap<String, String> newKeyValues = new HashMap<>();
        for (final Entry<String, String> entry : keyValues.entrySet()) {
            final String oKey = entry.getKey();
            final String oValue = entry.getValue();

            Objects.requireNonNull(oKey);
            Objects.requireNonNull(oValue);

            newKeyValues.put(prefixKey(oKey), oValue);
        }

        this.dssStore.put(newKeyValues);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#putSwap(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
            throws DynamicStatusStoreException {
        Objects.requireNonNull(newValue);
        return this.dssStore.putSwap(prefixKey(key), oldValue, newValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#putSwap(java.lang.String,
     * java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
            @NotNull Map<String, String> others) throws DynamicStatusStoreException {
        Objects.requireNonNull(newValue);
        Objects.requireNonNull(others);

        // *** Copy all the other keys and prefix them
        final HashMap<String, String> newOthers = new HashMap<>();
        for (final Entry<String, String> entry : others.entrySet()) {
            final String oKey = entry.getKey();
            final String oValue = entry.getValue();

            Objects.requireNonNull(oKey);
            Objects.requireNonNull(oValue);

            newOthers.put(prefixKey(oKey), oValue);
        }

        return this.dssStore.putSwap(prefixKey(key), oldValue, newValue, newOthers);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#get(java.lang.String)
     */
    @Override
    public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
        return this.dssStore.get(prefixKey(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#getPrefix(java.lang.
     * String)
     */
    @Override
    public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        final Map<String, String> gotSet = this.dssStore.getPrefix(prefixKey(keyPrefix));
        final HashMap<String, String> returnSet = new HashMap<>();

        for (Entry<String, String> entry : gotSet.entrySet()) {
            String key   = entry.getKey();
            final String value = entry.getValue();

            if (key.startsWith(this.prefix)) {
                key = key.substring(this.prefix.length());
                returnSet.put(key, value);
            } else {
                throw new DynamicStatusStoreException("Somehow we got keys with the wrong prefix");
            }
        }

        return returnSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#delete(java.lang.String)
     */
    @Override
    public void delete(@NotNull String key) throws DynamicStatusStoreException {
        this.dssStore.delete(prefixKey(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#delete(java.util.Set)
     */
    @Override
    public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
        Objects.requireNonNull(keys);

        // *** Copy all the keys and prefix them
        final HashSet<String> newKeys = new HashSet<>();
        for (final String key : keys) {
            Objects.requireNonNull(key);
            newKeys.add(prefixKey(key));
        }

        this.dssStore.delete(newKeys);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IDynamicStatusStoreKeyAccess#deletePrefix(java.lang.
     * String)
     */
    @Override
    public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        this.dssStore.deletePrefix(prefixKey(keyPrefix));
    }
    
    
    /**
     * <p>
     * Retrieve interface to control a dynamic resource represented in the framework
     * area. This is to allow the resource being managed to be automatically
     * represented on the Web UI and the Eclipse Automation Views.
     * </p>
     * 
     * <p>
     * The properties the framework create from will be
     * dss.framework.resource.namespace.resourceKey . After that the manager can set
     * the property names as necessary.
     * </p>
     * 
     * <p>
     * For example, if the zOS Security Manager is controlling a set of userids on
     * cluster PlexMA, the namespace is already set to 'zossec', the property key
     * would be 'PLEXMA.userid.JAT234'. This would result in the property
     * 'dss.framework.resource.zossec.PLEXMA.userid.JAT234=L3456'. The automation
     * views would build a tree view of the properties starting
     * 'dss.framework.resource'
     * </p>
     * 
     * @param key - The resource key to prefix the keys along with the namespace
     * @return A tailored IDynamicResource
     * @throws DynamicStatusStoreException
     */
    @Override
    public IDynamicResource getDynamicResource(String resourceKey) throws DynamicStatusStoreException {
        return new FrameworkDynamicResource();
    }

    /**
     * <p>
     * Retrieve an interface to update the Run status with manager related
     * information. This is information above what the framework would display, like
     * status, no. of methods etc.
     * </p>
     * 
     * <p>
     * One possible use would be the zOS Manager reporting the primary zOS Image the
     * test is running on.
     * </p>
     * 
     * @return The dynamic run resource tailored to this namespaces
     * @throws DynamicStatusStoreException
     */
    @Override
    public IDynamicRun getDynamicRun() throws DynamicStatusStoreException {
        return new FrameworkDynamicRun();
    }



    /**
     * Prefix the supplied key with the namespace
     *
     * @param key
     * @return
     */
    private String prefixKey(String key) {
        Objects.requireNonNull(key);
        return this.prefix + key;
    }
}
