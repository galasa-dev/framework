package io.ejat.framework.spi;

import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * <p>Used by the eJAT Framework to initialise the various Dynamic Status Stores that may exist within the OSGi instance.  Only 1 DSS maybe enabled during the lifetime of 
 * a eJAT test run or server instance.</p>
 * 
 * <p>The DSS should request from the framework the URI that is defined in the DSS.  It should examine the returned URI to 
 * determine if it is this DSS that is required to be initialised.  If the DSS should be initialised, the DSS should do so 
 * and then register itself in the Framework.
 *  
 * @author Michael Baylis
 *
 */
public interface IDynamicStatusStoreService {
	
	/**
	 * <p>This method is called to selectively initialise the DSS.  If this DSS is to be initialise, 
	 * it should register the DSS with @{link {@link io.ejat.framework.spi.IFrameworkInitialisation#registerDynamicStatusStore(IDynamicStatusStore)}</p> 
	 * 
	 * <p>If there is any problem initialising the sole DSS, then an exception will be thrown that will effectively terminate the Framework</p>
	 * 
	 * @param frameworkInitialisation - Initialisation object containing access to various initialisation methods
	 * @throws DynamicStatusStoreException - If there is a problem initialising the underlying
	 */
	@Null
	void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws DynamicStatusStoreException;
	
	/**
	 * Store a new key value pair in the server
	 * 
	 * @param key - the key to use, namespace already applied
	 * @param value - the value to use
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException; 
	
	/**
	 * Store multiple key/value pairs in the server, namespace already applied
	 * 
	 * @param keyValues - map of key/value pairs
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException;
	
	
	/**
	 * Put a key/value pair in the server if the key is set to the oldValue.
	 * 
	 * @param key - the key to use, namespace already applied
	 * @param oldValue - the value to compare with and must be equal to before the put is actioned.  Null means does not exist
	 * @param newValue - The new value to set the key to
	 * @return true if the put was actioned,  false if not.
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue) throws DynamicStatusStoreException;
	
	
	/**
	 * Put a key/value pair in the server if the key is set to the old value, along with a set of other key value pairs
	 * 
	 * @param key - the key to use, namespace already applied
	 * @param oldValue - the value to compare with and must be equal to before the put is actioned.  Null means does not exist
	 * @param newValue - The new value to set the key to
	 * @param others - other key/value pairs to put if the primary key is valid.
	 * @return true if the put was actioned, false if not 
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue, @NotNull Map<String, String> others) throws DynamicStatusStoreException;
	
	/**
	 * <p>Retrieves a string property from the Dynamic Status Store.</p>
	 * 
	 * @param key  The name of the property, namespace already applied.
	 * @return The value of the property,  can be null if it does not exist
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	@Null
	String get(@NotNull String key) throws DynamicStatusStoreException;


	/**
	 * Retrieve all values with this key prefix
	 * 
	 * @param keyPrefix - the prefix of all the keys to use, namespace already applied
	 * @return a map of key/value pairs
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	@NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException;
	
	/**
	 * Delete the key 
	 * 
	 * @param key - the key to use, namespace already applied
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	void delete(@NotNull String key) throws DynamicStatusStoreException;
	
	/**
	 * Delete a set of keys from the server
	 * 
	 * @param keys - all the keys that need to be deleted, namespace already applied
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException;
	
	/**
	 * Delete all keys with this prefix
	 * 
	 * @param keyPrefix - the prefix of all the keys to use, namespace already applied
	 * @throws DynamicStatusStoreException - If there is a problem accessing the underlying
	 */
	void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException;


}
