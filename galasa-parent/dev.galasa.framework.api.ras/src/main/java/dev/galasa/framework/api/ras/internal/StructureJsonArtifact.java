/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class StructureJsonArtifact implements IRunRootArtifact {

    @Override
    public byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException {
        TestStructure testStructure = run.getTestStructure();
        if (testStructure != null) {
            return RunArtifactsDownloadRoute.gson.toJson(testStructure).getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String getPathName() {
        return "/structure.json";
    }
}