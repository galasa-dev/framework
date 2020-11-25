/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2020.
 */
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
    private final String                         id;

    protected DirectoryRASRunResult(Path runDirectory, Gson gson, String id)
            throws JsonSyntaxException, JsonIOException, IOException {
        this.runDirectory = runDirectory;
        this.id           = id;

        Path structureFile = this.runDirectory.resolve("structure.json");
        
        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(structureFile))){
           this.testStructure = gson.fromJson(in, TestStructure.class);
        }
    

        this.fileSystemProvider = new DirectoryRASFileSystemProvider(this.runDirectory);
    }
    
    //for testing purposes
    protected DirectoryRASRunResult() {
    	this.testStructure = null;
    	this.runDirectory = null;
    	this.fileSystemProvider = null;
    	this.id                 = null;
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
            } catch (Exception e) {
                throw new ResultArchiveStoreException("Unable to read the run log at " + runLog.toString(), e);
            }
        }

        return "";
    }

    public void discard() throws ResultArchiveStoreException {
        //TODO
    }

    @Override
    public String getRunId() {
        return this.id;
    }
}
