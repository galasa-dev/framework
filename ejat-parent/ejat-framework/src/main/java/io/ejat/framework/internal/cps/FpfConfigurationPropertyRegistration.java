package io.ejat.framework.internal.cps;

import java.io.File;
import java.net.URI;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStoreRegistration;
import io.ejat.framework.spi.IFrameworkInitialisation;

/**

 * 
 * @author James Davies
 */

public class FpfConfigurationPropertyRegistration implements IConfigurationPropertyStoreRegistration {

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
				File file = new File(cps);

				if((!file.exists())){
					throw new ConfigurationPropertyStoreException("CPS file does not exsist");
				}
				if (isFileUri(cps)){
					frameworkInitialisation.registerConfigurationPropertyStore(new FpfConfigurationPropertyStore(cps));
				} 
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
