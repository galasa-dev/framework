package io.ejat.framework.spi;

import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * <p>Used to gain access to properties in the Dynamic Status Store</p>
 * 
 * <p>The framework will be configured with a single Dynamic Status Store where all the dynamic properties for run and resources are kept.</p>
 * 
 * <p>etcd3 is the preferred dynamic status store for eJAT</p>
 * 
 * <p>An {@link IDynamicStatusStoreKeyAccess} can be obtained from {@link IFramework#getDynamicStatusStore(String)}.
 * </p> 
 * 
 * @author Michael Baylis
 *
 */
public interface IDynamicStatusStoreKeyAccess {
	
	/**
	 * Store a new key value pair in the server
	 * 
	 * @param key - the key to use 
	 * @param value - the value to use
	 * @throws DynamicStatusStoreException
	 */
	void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException; 
	
	/**
	 * Store multiple key/value pairs in the server.
	 * 
	 * @param keyValues - map of key/value pairs
	 * @throws DynamicStatusStoreException
	 */
	void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException;
	
	
	/**
	 * Put a key/value pair in the server if the key is set to the oldValue.
	 * 
	 * @param key - the key to use
	 * @param oldValue - the value to compare with and must be equal to before the put is actioned.  Null means does not exist
	 * @param newValue - The new value to set the key to
	 * @return true if the put was actioned,  false if not.
	 * @throws DynamicStatusStoreException
	 */
	boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue) throws DynamicStatusStoreException;
	
	
	/**
	 * Put a key/value pair in the server if the key is set to the old value, along with a set of other key value pairs
	 * 
	 * @param key - the key to use
	 * @param oldValue - the value to compare with and must be equal to before the put is actioned.  Null means does not exist
	 * @param newValue - The new value to set the key to
	 * @param others - other key/value pairs to put if the primary key is valid.
	 * @return true if the 
	 * @throws DynamicStatusStoreException
	 */
	boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue, @NotNull Map<String, String> others) throws DynamicStatusStoreException;
	
	/**
	 * <p>Retrieves a string property from the Dynamic Status Store</p>
	 * 
	 * @param key  The name of the property.
	 * @return The value of the property,  can be null if it does not exist
	 * @throws DynamicStatusStoreException
	 */
	@Null
	String get(@NotNull String key) throws DynamicStatusStoreException;


	/**
	 * Retrieve all values with this key prefix
	 * 
	 * @param keyPrefix - the prefix of all the keys to use.
	 * @return
	 * @throws DynamicStatusStoreException
	 */
	@NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException;
	
	/**
	 * Delete the key 
	 * 
	 * @param key - the key to use
	 * @throws DynamicStatusStoreException
	 */
	void delete(@NotNull String key) throws DynamicStatusStoreException;
	
	/**
	 * Delete a set of keys from the server
	 * 
	 * @param keys - all the keys that need to be deleted
	 * @throws DynamicStatusStoreException
	 */
	void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException;
	
	/**
	 * Delete all keys with this prefix
	 * 
	 * @param keyPrefix - the prefix of all the keys to use.
	 * @throws DynamicStatusStoreException
	 */
	void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException;
}
