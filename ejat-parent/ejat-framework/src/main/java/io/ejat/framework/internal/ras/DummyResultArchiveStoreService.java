package io.ejat.framework.internal.ras;

import java.nio.file.FileSystem;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ITestStructure;
import io.ejat.framework.spi.ResultArchiveStoreException;

public class DummyResultArchiveStoreService implements IResultArchiveStoreService {

	@Override
	public void writeLog(@NotNull String message) {
		//Ignore this,  its a dummy
	}

	@Override
	public void writeLog(@NotNull List<String> messages) {
		//Ignore this,  its a dummy
	}

	@Override
	public void updateTestStructure(@NotNull ITestStructure testStructure) {
		//Ignore this,  its a dummy
	}

	@Override
	public FileSystem getStoredArtifactsFileSystem() {
		//Ignore this,  its a dummy
		return null;
	}

	/* (non-Javadoc)
	 * @see io.ejat.framework.spi.IResultArchiveStoreService#initialise(io.ejat.framework.spi.IFrameworkInitialisation)
	 */
	@Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ResultArchiveStoreException {
		// This is a dummy service,  so wont be registering itself,  created if no other RASs are available  
	}

}
