/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;

import org.osgi.service.component.annotations.ServiceScope;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import java.io.IOException;
import java.util.*;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/run/*/artifacts" }, name = "Galasa Runs artifact list microservice")
public class RunArtifactListServlet extends BaseServlet {


	private static final long serialVersionUID = 1L;

	final static Gson gson = GalasaGsonBuilder.build();

	private IFileSystem fileSystem;

	public RunArtifactListServlet(IFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	protected String retrieveResults( 
		Map<String, String[]> paramMap
	) throws InternalServletException {

		QueryParameters queryParams = new QueryParameters(paramMap);
		String runId = queryParams.getSingleString("runId", null);

		IRunResult run = null;
		try {
			run = getRunByRunId(runId);
		} catch (ResultArchiveStoreException e) {
			ServletError error = new ServletError(GAL5002_INVALID_RUN_ID,runId);
			throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
		}

		JsonArray artifactRecords = new JsonArray();
		try {
			// Walk through the artifact root directory, collecting each artifact and
			// filtering out all subdirectories
			fileSystem.walk(run.getArtifactsRoot())
				.filter(fileSystem::isRegularFile)
				.forEach(artifactPath -> {
					JsonObject artifactRecord = new JsonObject();

					artifactRecord.add("runId", new JsonPrimitive(runId));
					artifactRecord.add("path", new JsonPrimitive(artifactPath.toString()));
					artifactRecord.add("url", new JsonPrimitive(artifactPath.toString()));
	
					artifactRecords.add(artifactRecord);					
				});
	
		} catch( ResultArchiveStoreException | IOException ex ) {
			ServletError error = new ServletError(GAL5007_ERROR_RETRIEVING_ARTIFACTS,runId);
			throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		String returnedJsonString = gson.toJson(artifactRecords);
		return returnedJsonString;
	}


	private IRunResult getRunByRunId(String id) throws ResultArchiveStoreException {

		IRunResult run = null;

		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

			run = directoryService.getRunById(id);

			if(run != null) {
				return run;
			}
		}
		return null;
	}
}
