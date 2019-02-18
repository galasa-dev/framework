package io.ejat.framework.internal.cps;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IFrameworkInitialisation;

@Component(service= {IConfigurationPropertyStoreService.class})
public class FpfConfigurationPropertyStoreService implements IConfigurationPropertyStoreService {

	@Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
			throws ConfigurationPropertyStoreException {
		// Temporary empty to test the SCR in Karaf
	}

	@Override
	public @Null String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
		// Temporary empty to test the SCR in Karaf
		return null;
	}

}
