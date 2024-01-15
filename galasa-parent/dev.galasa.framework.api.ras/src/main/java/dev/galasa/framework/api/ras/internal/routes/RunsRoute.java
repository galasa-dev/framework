/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.ResultNames;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;


/**
 * An abstract route used by all the Run-related routes.
 */
public abstract class RunsRoute extends BaseRoute {

    static final Gson gson = GalasaGsonBuilder.build();

    // Define a default filter to accept everything
    static DirectoryStream.Filter<Path> defaultFilter = path -> { return true; };

    private IFramework framework;

    public RunsRoute(ResponseBuilder responseBuilder, String path , IFramework framework ) {
        super(responseBuilder, path);
        this.framework = framework;
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    protected List<String> getResultNames () throws InternalServletException{
		List<String> resultsList = new ArrayList<>();

		try {
			for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
                List<String> results = directoryService.getResultNames();
                if (results != null){
				resultsList.addAll(directoryService.getResultNames());
                }
			}
            for (String defaultResultName : ResultNames.getDefaultResultNames()){
				if (!resultsList.contains(defaultResultName)){
					resultsList.add(defaultResultName);
				}
		    }
		}
        catch(ResultArchiveStoreException r){
            ServletError error = new ServletError(GAL5004_ERROR_RETRIEVING_PAGE );
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, r);
        }

		Collections.sort(resultsList);

		return resultsList;
	}


    protected IRunResult getRunByRunId(String id) throws ResultArchiveStoreException {
        IRunResult run = null;

        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

            run = directoryService.getRunById(id);

            if (run != null) {
                return run;
            }
        }
        return null;
    }

    protected List<String> getRequestors() throws ResultArchiveStoreException{
		HashSet<String> requestorSet = new HashSet<>();
		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
			requestorSet.addAll(directoryService.getRequestors());
		}
		//convert to list of strings
		List<String> requestors = new ArrayList<>(requestorSet);
		return requestors;
	}

    protected List<RasTestClass> getTestClasses() throws ResultArchiveStoreException, ServletException{
        List<RasTestClass> testClasses = new ArrayList<>();
        for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
            testClasses.addAll(directoryService.getTests());
        }
        return testClasses;
    }
}