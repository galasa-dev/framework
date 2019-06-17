package dev.voras.framework.internal.cps;

import java.io.File;
import java.net.URI;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.IConfigurationPropertyStoreRegistration;
import dev.voras.framework.spi.IFrameworkInitialisation;

/**

 * 
 * @author James Davies
 */
@Component(service= {IConfigurationPropertyStoreRegistration.class})
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
				
				if (!cps.getScheme().equals("file")) {
					return;
				}
				
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
