/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;
import static dev.galasa.framework.api.ras.internal.BaseServlet.*;

/**
 * Implementation to retrieve a list of artifacts for a given run based on its runId.
 */
public class RunArtifactsListRoute extends RunsRoute {

    static final Gson gson = GalasaGsonBuilder.build();

    private List<IRunRootArtifact> rootArtifacts = new ArrayList<>();

    public RunArtifactsListRoute(IFileSystem fileSystem, IFramework framework) {
        //  Regex to match endpoint: /ras/runs/{runId}/artifacts
        super("\\/runs\\/([A-z0-9.\\-=]+)\\/artifacts\\/?", fileSystem, framework);
        rootArtifacts = Arrays.asList(new RunLogArtifact(), new StructureJsonArtifact(), new ArtifactPropertiesArtifact(this));
    }

    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse res) throws ServletException, IOException, FrameworkException {
        Matcher matcher = Pattern.compile(this.getPath()).matcher(pathInfo);
        matcher.matches();
        String runId = matcher.group(1);
        return sendResponse(res, retrieveResults(runId), HttpServletResponse.SC_OK);
    }

    private String retrieveResults(String runId) throws InternalServletException {
        IRunResult run = null;
        JsonArray artifacts = new JsonArray();
        try {
            run = getRunByRunId(runId);
        } catch (ResultArchiveStoreException e) {
            ServletError error = new ServletError(GAL5002_INVALID_RUN_ID, runId);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }

        // Build a JSON array of artifacts, then return it as a JSON string
        try {
            artifacts = getArtifacts(run);
            artifacts.addAll(getRootArtifacts(run));
        } catch (ResultArchiveStoreException | IOException ex) {
            ServletError error = new ServletError(GAL5007_ERROR_RETRIEVING_ARTIFACTS_LIST, runId);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return gson.toJson(artifacts);
    }

    private JsonArray getRootArtifacts(IRunResult run) throws ResultArchiveStoreException, IOException {
        JsonArray artifactRecords = new JsonArray();
        for (IRunRootArtifact rootArtifact : rootArtifacts) {
            byte[] content = rootArtifact.getContent(run);
            if (content != null) {
                artifactRecords.add(getArtifactAsJsonObject(rootArtifact.getPathName(), rootArtifact.getContentType(), content.length));
            }
        }
        return artifactRecords;
    }
}
