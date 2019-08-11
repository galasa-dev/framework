package dev.voras.framework.internal.cps;

import java.net.URI;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.FrameworkPropertyFile;
import dev.voras.framework.spi.FrameworkPropertyFileException;
import dev.voras.framework.spi.IConfigurationPropertyStore;


/**
 *  <p>This class is used when the FPF class is being operated as the Key-Value store for the Configuration
 *  property store. This class registers the Configuration property store as the only CPS.</p>
 * 
 * @author James Davies
 */

public class FpfConfigurationPropertyStore implements IConfigurationPropertyStore {
	private FrameworkPropertyFile fpf;

	public FpfConfigurationPropertyStore(URI file) throws ConfigurationPropertyStoreException {
		try {
			fpf = new FrameworkPropertyFile(file);
		} catch (FrameworkPropertyFileException e) {
			throw new ConfigurationPropertyStoreException("Failed to create Framework property file", e);
		}
	}

	/**
	 * <p>This method implements the getProperty method from the framework property file class, returning a string 
	 * value from a key inside the property file, or null if empty.</p>
	 * 
	 * @param String key
	 * @throws ConfigurationPropertyStoreException
	 */
	@Override
	public @Null String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
		return fpf.get(key);
	}

	/**
	 * <p>A simple method thta checks the provided URI to the CPS is a local file or not.</p>
	 * 
	 * @param uri - URI to the CPS
	 * @return - boolean if File or not.
	 */
	public static boolean isFileUri(URI uri) {
		return "file".equals(uri.getScheme());
	}

	@Override
	public void shutdown() throws ConfigurationPropertyStoreException {
		try {
			this.fpf.shutdown();
		} catch (FrameworkPropertyFileException e) {
			throw new ConfigurationPropertyStoreException("Problem shutting down the CPS File", e);
		}
	}
}
