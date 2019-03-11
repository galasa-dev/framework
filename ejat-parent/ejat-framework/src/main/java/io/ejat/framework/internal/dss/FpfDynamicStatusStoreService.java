package io.ejat.framework.internal.dss;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.FrameworkPropertyFileException;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFrameworkInitialisation;

/**
 *  <p>This class is used when the FPF class is being operated as the Key-Value store for the Dynamic Status Store. 
 *  This class registers the Dynamic Status Store as the only DSS.</p>
 * 
 * @author Bruce Abbott
 */

@Component(service= {IDynamicStatusStoreService.class})
public class FpfDynamicStatusStoreService implements IDynamicStatusStoreService {
	private FrameworkPropertyFile fpf;

	/**
	 * <p>This method checks that the DSS is a local file, and if true registers this file as the ONLY DSS.</p>
	 * 
	 * @param frameworkInitialisation
	 * @throws DynamicStatusStoreException
	 */
	@Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
			throws DynamicStatusStoreException {
		URI dss = frameworkInitialisation.getDynamicStatusStoreUri();
		if (isFileUri(dss)) {
			try {
				fpf = new FrameworkPropertyFile(dss);
				frameworkInitialisation.registerDynamicStatusStoreService(this);
			} catch (FrameworkPropertyFileException e ) {
				throw new DynamicStatusStoreException("Could not initialise Framework Property File", e);
			}
		}
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

	/**
	 * <p>A simple method that checks the provided URI to the CPS is a local file or not.</p>
	 * 
	 * @param uri - URI to the CPS
	 * @return - boolean if File or not.
	 */
	protected static boolean isFileUri(URI uri) {
		return "file".equals(uri.getScheme());
	}
}
