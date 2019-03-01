package io.ejat.framework.internal.ras;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ITestStructure;
import io.ejat.framework.spi.ResultArchiveStoreException;

public class DirectoryResultArchiveStoreService implements IResultArchiveStoreService {
	
	private IFramework framework;
	private URI        rasUri;
	private File       baseDirectory;
	
	private File       runDirectory;
	
	private FileSystem artifactFileSystem;
	
	@Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
			throws ResultArchiveStoreException {
		this.framework = frameworkInitialisation.getFramework();
		
		List<URI> rasUris = frameworkInitialisation.getResultArchiveStoreUris();
		for(URI uri : rasUris) {
			if ("file".equals(uri.getScheme())) {
				if (rasUri != null) {
					throw new ResultArchiveStoreException("The Directory RAS currently does not support multiple instances of itself");
				}
				rasUri = uri;
			}
		}
		
		baseDirectory = Paths.get(rasUri).toFile();
		if (!baseDirectory.exists()) {
			if (!baseDirectory.mkdirs()) {
				throw new ResultArchiveStoreException("Unable to create the base RAS base directory '" + baseDirectory.toURI() + "'");
			}
		}
		
		String runuuid = this.framework.getTestRunId();
		if (runuuid != null) {
			setRasRun(runuuid, this.framework.getTestRunName());
		}
		
		frameworkInitialisation.registerResultArchiveStoreService(this);		
	}

	private void setRasRun(String runuuid, String testRunName) throws ResultArchiveStoreException {
		runDirectory = baseDirectory.toPath().resolve(runuuid).toFile();
		if (!runDirectory.exists()) {
			if (!runDirectory.mkdirs()) {
				throw new ResultArchiveStoreException("Unable to create the base RAS run directory '" + runDirectory.toURI() + "'");
			}
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
