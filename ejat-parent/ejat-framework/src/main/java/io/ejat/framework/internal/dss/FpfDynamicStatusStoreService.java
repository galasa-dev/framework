package io.ejat.framework.internal.dss;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFrameworkInitialisation;

@Component(service= {IDynamicStatusStoreService.class})
public class FpfDynamicStatusStoreService implements IDynamicStatusStoreService {

	@Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
			throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
	}

}
