/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static dev.galasa.framework.api.ras.internal.verycommon.ServletErrorMessage.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.verycommon.*;
import dev.galasa.framework.api.ras.internal.common.SortQueryParameterChecker;
import dev.galasa.framework.api.ras.internal.verycommon.InternalServletException;
import dev.galasa.framework.api.ras.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.ras.internal.verycommon.ResponseBuilder;
import dev.galasa.framework.api.ras.internal.verycommon.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class ResultNamesRoute extends RunsRoute {

	public ResultNamesRoute(ResponseBuilder responseBuilder, IFramework framework) {
		/* Regex to match endpoints: 
		*  -> /ras/runs
		*  -> /ras/runs/
		*  -> /ras/runs?{querystring} 
		*/
		super(responseBuilder, "\\/resultnames?", framework);
	}

	final static Gson gson = GalasaGsonBuilder.build();
    private SortQueryParameterChecker sortQueryParameterChecker = new SortQueryParameterChecker();

    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String outputString = retrieveResults(queryParams);
		return getResponseBuilder().buildResponse(response, "application/json", outputString, HttpServletResponse.SC_OK); 
    }

    public String retrieveResults (QueryParameters queryParams) throws ServletException, InternalServletException{
        List<String> resultsList = getResultNames();

		try {
            if (queryParams.getSingleString("sort", null) !=null ){
			    if (!sortQueryParameterChecker.isAscending(queryParams, "resultnames")) {
				    Collections.reverse(resultsList);
                }
			}
		} catch (InternalServletException e){
			ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED, "resultnames");
			throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
		}

		JsonElement json = new Gson().toJsonTree(resultsList);
		JsonObject resultnames = new JsonObject();
		resultnames.add("resultnames", json);
		return resultnames.toString();
    }

}
