package io.ejat.framework.internal.ras;

import java.nio.file.FileSystem;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IResultArchiveStore;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ITestStructure;

public class FrameworkResultArchiveStore implements IResultArchiveStore {
	
	private final List<IResultArchiveStoreService> rasServices;
	
	public FrameworkResultArchiveStore(IFramework framework, List<IResultArchiveStoreService> rasServices) {
		this.rasServices = rasServices;
		
		// If there are no RAS services, create a dummy one to make the code cleaner
		if (this.rasServices.isEmpty()) {
			this.rasServices.add(new DummyResultArchiveStoreService());
		}
	}

	@Override
	public void writeLog(@NotNull String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeLog(@NotNull List<String> messages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTestStructure(@NotNull ITestStructure testStructure) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FileSystem getStoredArtifactsFileSystem() {
		// TODO Auto-generated method stub
		return null;
	}

}
