package io.ejat.framework.internal.cps;

import java.net.URI;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.FrameworkPropertyFileException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IFrameworkInitialisation;

/**
 *  <p>This class is used when the FPF class is being operated as the Key-Value store for the Configuration
 *  property store. This class registers the Configuration property store as the only CPS.</p>
 * 
 * @author James Davies
 */

@Component(service= {IConfigurationPropertyStoreService.class})
public class FpfConfigurationPropertyStoreService implements IConfigurationPropertyStoreService {
	private FrameworkPropertyFile fpf;

	/**
	 * <p>This method checks that the CPS is a local file, and if true registers this file as the ONLY CPS.</p>
	 * 
	 * @param IFrameworkInitialisation
	 * @throws ConfigurationPropertyStoreException
	 */
	@Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
			throws ConfigurationPropertyStoreException {
				URI cps = frameworkInitialisation.getBootstrapConfigurationPropertyStore();
				if (isFileUri(cps)){
					try {
						fpf = new FrameworkPropertyFile(cps);
						frameworkInitialisation.registerConfigurationPropertyStoreService(this);
					} catch (FrameworkPropertyFileException e ){
						throw new ConfigurationPropertyStoreException("Could not intialise Framework Property File", e);
					}
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
}
