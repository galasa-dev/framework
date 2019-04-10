package io.ejat.framework.internal.dss;

import java.net.URI;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IDynamicStatusStoreRegistration;
import io.ejat.framework.spi.IFrameworkInitialisation;

/**

 * 
 * @author Bruce Abbott
 */
@Component(service= {IDynamicStatusStoreRegistration.class})
public class FpfDynamicStatusStoreRegistration implements IDynamicStatusStoreRegistration {

	/**
	 * <p>This method registers this as the only DSS Store.</p>
	 * 
	 * @param IFrameworkInitialisation
	 * @throws DynamicStatusStoreException
	 */
    @Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
			throws DynamicStatusStoreException {
        URI dss = frameworkInitialisation.getDynamicStatusStoreUri();
		if (isFileUri(dss)) {
			frameworkInitialisation.registerDynamicStatusStore(new FpfDynamicStatusStore(dss));
		}
		else {
			throw new DynamicStatusStoreException("Could not initialise Framework Property File");
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