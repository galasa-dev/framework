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

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.FrameworkPropertyFileException;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IDynamicStatusStore;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IFramework;


/**
 *  <p>This class is used when the FPF class is being operated as the Key-Value store for the Dynamic Status Store. 
 *  This class registers the Dynamic Status Store as the only DSS.</p>
 * 
 * @author Bruce Abbott
 */

public class FrameworkDynamicStatusStoreService implements IDynamicStatusStoreService {
	private FrameworkPropertyFile fpf;

	private final IDynamicStatusStore 		 dssStore;
    private final String                     namespace;
	private final String                     prefix;
	
	public FrameworkDynamicStatusStoreService(IFramework framework, IDynamicStatusStore dssStore, String namespace) {
        Objects.requireNonNull(dssStore);
        Objects.requireNonNull(namespace);

        this.dssStore = dssStore;
        this.namespace = namespace;
        this.prefix = "dss." + this.namespace + ".";
    }

	// /**
	//  * <p>This method checks that the DSS is a local file, and if true registers this file as the ONLY DSS.</p>
	//  * 
	//  * @param frameworkInitialisation
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
	// 		throws DynamicStatusStoreException {
	// 	URI dss = frameworkInitialisation.getDynamicStatusStoreUri();
	// 	if (isFileUri(dss)) {
	// 		try {
	// 			fpf = new FrameworkPropertyFile(dss);
	// 			frameworkInitialisation.registerDynamicStatusStoreService(this);
	// 		} catch (FrameworkPropertyFileException e ) {
	// 			throw new DynamicStatusStoreException("Could not initialise Framework Property File", e);
	// 		}
	// 	}
	// }

	// /**
	//  * <p>This method puts a key/value pair in the DSS.</p>
	//  * 
	//  * @param key
	//  * @param value
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
	// 	try {
	// 		fpf.set(key, value);
	// 	} catch (FrameworkPropertyFileException e) {
	// 		throw new DynamicStatusStoreException("Unable to put key/value pair", e);
	// 	}
		
	// }

	// /**
	//  * <p>This method puts multiple key/value pairs in the DSS.</p>
	//  * 
	//  * @param keyValues
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
	// 	try {
	// 		fpf.set(keyValues);
	// 	} catch (FrameworkPropertyFileException | IOException e) {
	// 		throw new DynamicStatusStoreException("Unable to put map of key/value pairs", e);
	// 	}
		
	// }

	// /**
	//  * <p>This method swaps an old value with a new value for a given key in the DSS.</p>
	//  * 
	//  * @param key
	//  * @param oldValue
	//  * @param newValue
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
	// 		throws DynamicStatusStoreException {
	// 	try {
	// 		return fpf.setAtomic(key, oldValue, newValue);
	// 	} catch (FrameworkPropertyFileException e) {
	// 		throw new DynamicStatusStoreException("Unable to swap old value for new value with given key", e);
	// 	}
	// }

	// /**
	//  * <p>This method swaps an old value with a new value for a given key and puts multiple key/value pairs in the DSS.</p>
	//  * 
	//  * @param key
	//  * @param oldValue
	//  * @param newValue
	//  * @param others
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
	// 		@NotNull Map<String, String> others) throws DynamicStatusStoreException {
	// 	try {
	// 		return fpf.setAtomic(key, oldValue, newValue, others);
	// 	} catch (FrameworkPropertyFileException e) {
	// 		throw new DynamicStatusStoreException("Unable to swap old value for new value and put map of key/value pairs with given key", e);
	// 	}
	// }

	// /**
	//  * <p>This method gets a value from a given key in the DSS.</p>
	//  * 
	//  * @param key
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
	// 	return fpf.get(key);	
	// }

	// /**
	//  * <p>This method gets all key/value pairs with a given key prefix from the DSS.</p>
	//  * 
	//  * @param keyPrefix
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
	// 	try {
	// 		return fpf.getPrefix(keyPrefix);
	// 	} catch (Exception e) {
	// 		throw new DynamicStatusStoreException("Unable to get map of key/value pairs with given key prefix", e);
	// 	}
	// }

	// /**
	//  * <p>This method deleted a key/value pair with a given key from the DSS.</p>
	//  * 
	//  * @param key
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public void delete(@NotNull String key) throws DynamicStatusStoreException {
	// 	try {
	// 		fpf.delete(key);
	// 	} catch (FrameworkPropertyFileException e) {
	// 		throw new DynamicStatusStoreException("Unable to delete key/value pair with given key", e);
	// 	}
	// }

	// /**
	//  * <p>This method deletes multiple key/value pairs with the given keys from the DSS.</p>
	//  * 
	//  * @param keys
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
	// 	try {
	// 		fpf.delete(keys);
	// 	} catch (FrameworkPropertyFileException e) {
	// 		throw new DynamicStatusStoreException("Unable to delete key/value pairs with given keys", e);
	// 	}
	// }

	// /**
	//  * <p>This method deletes multiple key/value pairs with a given key prefix from the DSS.</p>
	//  * 
	//  * @param keyPrefix
	//  * @throws DynamicStatusStoreException
	//  */
	// @Override
	// public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
	// 	try {
	// 		fpf.deletePrefix(keyPrefix);
	// 	} catch (FrameworkPropertyFileException e) {
	// 		throw new DynamicStatusStoreException("Unable to delete key/value pairs with given key prefix", e);
	// 	}
	// }

	// /**
	//  * <p>A simple method that checks the provided URI to the CPS is a local file or not.</p>
	//  * 
	//  * @param uri - URI to the CPS
	//  * @return - boolean if File or not.
	//  */
	// protected static boolean isFileUri(URI uri) {
	// 	return "file".equals(uri.getScheme());
	// }

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
