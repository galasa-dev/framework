/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.commons;

import java.io.IOException;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public interface IRunRootArtifact {
    String getPathName();
    byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException;
    String getContentType();
}
