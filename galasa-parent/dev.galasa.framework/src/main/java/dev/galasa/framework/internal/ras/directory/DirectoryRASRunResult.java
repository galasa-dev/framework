package dev.galasa.framework.internal.ras.directory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class DirectoryRASRunResult implements IRunResult {
	
	private final Path                           runDirectory;
	private final TestStructure                  testStructure;
	private final DirectoryRASFileSystemProvider fileSystemProvider;
	
	protected DirectoryRASRunResult(Path runDirectory, Gson gson) throws JsonSyntaxException, JsonIOException, IOException {
		this.runDirectory = runDirectory;
		
		Path structureFile = this.runDirectory.resolve("structure.json");
		this.testStructure = gson.fromJson(new InputStreamReader(Files.newInputStream(structureFile)), TestStructure.class);
		
		this.fileSystemProvider = new DirectoryRASFileSystemProvider(this.runDirectory);
	}

	@Override
	public TestStructure getTestStructure() throws ResultArchiveStoreException {
		return this.testStructure;
	}

	@Override
	public Path getArtifactsRoot() throws ResultArchiveStoreException {
		return this.fileSystemProvider.getActualFileSystem().getPath("/");
	}

	@Override
	public String getLog() throws ResultArchiveStoreException {
		
		Path runLog = runDirectory.resolve("run.log");
		if (Files.exists(runLog)) {
			try {
				return new String(Files.readAllBytes(runLog));
			} catch(Exception e) {
				throw new ResultArchiveStoreException("Unable to read the run log at " + runLog.toString(), e);
			}
		}
		
		return "";
	}
}
