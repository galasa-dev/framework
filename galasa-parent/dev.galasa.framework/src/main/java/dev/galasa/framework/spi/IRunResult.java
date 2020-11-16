/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2020.
 */
package dev.galasa.framework.spi;

import java.nio.file.Path;

import dev.galasa.framework.spi.teststructure.TestStructure;

public interface IRunResult {
    
    String getRunId();

    TestStructure getTestStructure() throws ResultArchiveStoreException;

    Path getArtifactsRoot() throws ResultArchiveStoreException;

    String getLog() throws ResultArchiveStoreException;

    void discard() throws ResultArchiveStoreException;

}
