package dev.voras.framework.spi;

import java.nio.file.Path;

import dev.voras.framework.spi.teststructure.TestStructure;

public interface IRunResult {
	
	TestStructure getTestStructure() throws ResultArchiveStoreException;
	
	Path getArtifactsRoot() throws ResultArchiveStoreException;
	
	String getLog() throws ResultArchiveStoreException;

}
