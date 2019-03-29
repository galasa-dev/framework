package io.ejat.framework.internal.dss;

import java.io.File;
import java.net.URI;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IDynamicStatusStoreRegistration;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.FrameworkPropertyFileException;

public class FpfDynamicStatusStoreRegistration implements IDynamicStatusStoreRegistration {

    @Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
			throws DynamicStatusStoreException {
        URI dss = frameworkInitialisation.getDynamicStatusStoreUri();
		if (isFileUri(dss)) {
			try {
				//fpf = new FrameworkPropertyFile(dss);
				frameworkInitialisation.registerDynamicStatusStore(new FpfDynamicStatusStore(dss));
			} catch (Exception e) {
				throw new DynamicStatusStoreException("Could not initialise Framework Property File", e);
			}
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