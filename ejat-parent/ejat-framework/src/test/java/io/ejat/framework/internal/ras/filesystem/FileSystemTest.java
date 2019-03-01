package io.ejat.framework.internal.ras.filesystem;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileSystemTest {
	
	private Path rasDirectory;
	
	@Before
	public void before() throws IOException {
		this.rasDirectory = Files.createTempDirectory("ejat_junit_rasfs_");		
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
	public void testSsimpleFile() throws IOException {
		FileSystem fs = new DirectoryResultArchiveStoreFileSystem(rasDirectory);
		
		Assert.assertFalse("Must not be read only", fs.isReadOnly());
		Assert.assertTrue("Must be open", fs.isOpen());
		Assert.assertEquals("Invalid separater", "/", fs.getSeparator());
		Assert.assertNotNull("Did not return a list of filestores", fs.getFileStores());
		
	}
	

}
