package io.ejat.framework.internal.dss;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

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

	@Override
	public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
	}

	@Override
	public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
	}

	@Override
	public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
			throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
		return false;
	}

	@Override
	public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
			@NotNull Map<String, String> others) throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
		return false;
	}

	@Override
	public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
		return null;
	}

	@Override
	public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
		return new HashMap<>();
	}

	@Override
	public void delete(@NotNull String key) throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
	}

	@Override
	public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
	}

	@Override
	public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
		// Temporary empty to test the SCR in Karaf
	}

}
