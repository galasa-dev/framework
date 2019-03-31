package io.ejat.framework.internal.dss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.net.URI;
import java.io.IOException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IDynamicResource;
import io.ejat.framework.spi.IDynamicRun;
import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.FrameworkPropertyFileException;

/**
 * <p>
 * A DSS Stub which is passed to the managers that will pass the requests to the
 * activated DSS Service
 * </p>
 *
 * @author Bruce Abbott
 *
 */
public class FpfDynamicStatusStore implements IDynamicStatusStore {

    private FrameworkPropertyFile fpf;
    // private final IDynamicStatusStoreService dssService;
    // private final String                     namespace;
    // private final String                     prefix;

    /**
     * <p>
     * Temp
     * </p>
     *
     * @param framework  - not currently used.
     * @param dssService - the registered service for the DSS
     * @param namespace  - The namespace for keys for a specfic manager
     */
    public FpfDynamicStatusStore(URI file) throws DynamicStatusStoreException {
        try {
			fpf = new FrameworkPropertyFile(file);
		} catch (FrameworkPropertyFileException e) {
			throw new DynamicStatusStoreException("Failed to create Framework property file", e);
		}
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
	 * <p>This method puts a key/value pair in the DSS.</p>
	 * 
	 * @param key
	 * @param value
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
		try {
			fpf.set(key, value);
		} catch (FrameworkPropertyFileException e) {
			throw new DynamicStatusStoreException("Unable to put key/value pair", e);
		}
		
	}

	/**
	 * <p>This method puts multiple key/value pairs in the DSS.</p>
	 * 
	 * @param keyValues
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
		try {
			fpf.set(keyValues);
		} catch (FrameworkPropertyFileException | IOException e) {
			throw new DynamicStatusStoreException("Unable to put map of key/value pairs", e);
		}
		
	}

	/**
	 * <p>This method swaps an old value with a new value for a given key in the DSS.</p>
	 * 
	 * @param key
	 * @param oldValue
	 * @param newValue
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
			throws DynamicStatusStoreException {
		try {
			return fpf.setAtomic(key, oldValue, newValue);
		} catch (FrameworkPropertyFileException e) {
			throw new DynamicStatusStoreException("Unable to swap old value for new value with given key", e);
		}
	}

	/**
	 * <p>This method swaps an old value with a new value for a given key and puts multiple key/value pairs in the DSS.</p>
	 * 
	 * @param key
	 * @param oldValue
	 * @param newValue
	 * @param others
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
			@NotNull Map<String, String> others) throws DynamicStatusStoreException {
		try {
			return fpf.setAtomic(key, oldValue, newValue, others);
		} catch (FrameworkPropertyFileException e) {
			throw new DynamicStatusStoreException("Unable to swap old value for new value and put map of key/value pairs with given key", e);
		}
	}

	/**
	 * <p>This method gets a value from a given key in the DSS.</p>
	 * 
	 * @param key
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
		return fpf.get(key);	
	}

	/**
	 * <p>This method gets all key/value pairs with a given key prefix from the DSS.</p>
	 * 
	 * @param keyPrefix
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
		try {
			return fpf.getPrefix(keyPrefix);
		} catch (Exception e) {
			throw new DynamicStatusStoreException("Unable to get map of key/value pairs with given key prefix", e);
		}
	}

	/**
	 * <p>This method deleted a key/value pair with a given key from the DSS.</p>
	 * 
	 * @param key
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public void delete(@NotNull String key) throws DynamicStatusStoreException {
		try {
			fpf.delete(key);
		} catch (FrameworkPropertyFileException e) {
			throw new DynamicStatusStoreException("Unable to delete key/value pair with given key", e);
		}
	}

	/**
	 * <p>This method deletes multiple key/value pairs with the given keys from the DSS.</p>
	 * 
	 * @param keys
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
		try {
			fpf.delete(keys);
		} catch (FrameworkPropertyFileException e) {
			throw new DynamicStatusStoreException("Unable to delete key/value pairs with given keys", e);
		}
	}

	/**
	 * <p>This method deletes multiple key/value pairs with a given key prefix from the DSS.</p>
	 * 
	 * @param keyPrefix
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
		try {
			fpf.deletePrefix(keyPrefix);
		} catch (FrameworkPropertyFileException e) {
			throw new DynamicStatusStoreException("Unable to delete key/value pairs with given key prefix", e);
		}
	}
}