/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.commons;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public class RunLogArtifact implements IRunRootArtifact {

    @Override
    public byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException {
        String runLog = run.getLog();
        if (runLog != null) {
            return runLog.getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public String getPathName() {
        return "/run.log";
    }
}