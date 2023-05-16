/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal.commons;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;

import dev.galasa.framework.api.ras.internal.routes.RunsRoute;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public class ArtifactPropertiesArtifact implements IRunRootArtifact {

    private RunsRoute runsRoute;

    public ArtifactPropertiesArtifact(RunsRoute runsRoute) {
        this.runsRoute = runsRoute;
    }

    @Override
    public byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException {
        JsonArray artifactProperties = runsRoute.getArtifacts(run);
        return artifactProperties.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public String getPathName() {
        return "/artifacts.properties";
    }
}
