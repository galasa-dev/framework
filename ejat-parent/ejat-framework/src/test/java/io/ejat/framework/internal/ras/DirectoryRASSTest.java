package io.ejat.framework.internal.ras;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.ResultArchiveStoreException;

public class DirectoryRASSTest {
	
	private Path rasDirectory;
	
	private IFramework               framework;
	private IFrameworkInitialisation frameworkInit;
	
	private static final String runid   = "areallygoodid";
	private static final String runname = "BOB1";
	
	@Before
	public void before() throws IOException {
		this.rasDirectory = Files.createTempDirectory("ejat_junit_ras_");
		
		this.framework = mock(IFramework.class);
		when(this.framework.getTestRunId()).thenReturn(runid);
		when(this.framework.getTestRunName()).thenReturn(runname);
		
		ArrayList<URI> rasURIs = new ArrayList<>();
		rasURIs.add(this.rasDirectory.toUri());
		
		this.frameworkInit = mock(IFrameworkInitialisation.class);
		when(this.frameworkInit.getResultArchiveStoreUris()).thenReturn(rasURIs);
		when(this.frameworkInit.getFramework()).thenReturn(framework);
	}
	
	@After
	public void after() throws IOException {
		if (this.rasDirectory != null) {
			if (Files.exists(this.rasDirectory)) {
				FileUtils.deleteDirectory(this.rasDirectory.toFile());
			}
		}
	}
	
	@Test
	public void testFileSystemWrite() throws ResultArchiveStoreException {
		DirectoryResultArchiveStoreService drass = new DirectoryResultArchiveStoreService();
		drass.initialise(this.frameworkInit);
		verify(this.frameworkInit).registerResultArchiveStoreService(drass);
		
		Assert.assertTrue("Run RAS directory should have been created", Files.exists(rasDirectory.resolve(runid)));
		
		FileSystem rasFS = drass.getStoredArtifactsFileSystem();
		Assert.assertNotNull("RASS did not return a FileSystem", rasFS);
	}

}
