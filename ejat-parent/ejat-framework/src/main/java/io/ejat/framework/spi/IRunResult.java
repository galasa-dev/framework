package io.ejat.framework.spi;

import java.nio.file.Path;

import io.ejat.framework.spi.teststructure.TestStructure;

public interface IRunResult {
	
	TestStructure getTestStructure() throws ResultArchiveStoreException;
	
	Path getArtifactsRoot() throws ResultArchiveStoreException;

}
