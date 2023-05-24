/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import dev.galasa.framework.api.ras.internal.routes.RunsRoute;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class ArtifactsJson implements IRunRootArtifact {

    private RunsRoute runsRoute;
    static final Gson gson = GalasaGsonBuilder.build();

    public ArtifactsJson(RunsRoute runsRoute) {
        this.runsRoute = runsRoute;
    }

    @Override
    public byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException {
        JsonArray artifactsJson = runsRoute.getArtifacts(run);
        return gson.toJson(artifactsJson).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String getPathName() {
        return "/artifacts.json";
    }
}
