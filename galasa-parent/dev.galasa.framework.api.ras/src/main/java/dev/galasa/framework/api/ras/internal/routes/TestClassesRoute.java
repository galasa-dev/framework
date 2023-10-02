/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.common.RasQueryParameters;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class TestClassesRoute extends RunsRoute {

    public TestClassesRoute(ResponseBuilder responseBuilder, String path, IFramework framework) {
        /* Regex to match endpoints: 
		*  -> /ras/testclasses
		*  -> /ras/testclasses?
		*/
        super(responseBuilder, "\\/testclasses[?]?", framework);
    }

    final static Gson gson = GalasaGsonBuilder.build();
    private RasQueryParameters sortQueryParameterChecker;

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,HttpServletRequest req, HttpServletResponse response)
    throws ServletException, IOException, FrameworkException {
        this.sortQueryParameterChecker = new RasQueryParameters(queryParams);
        String outputString = TestClasses();
        return getResponseBuilder().buildResponse(response, "application/json", outputString, HttpServletResponse.SC_OK); 
    }
    
    private String TestClasses () throws ResultArchiveStoreException, ServletException, InternalServletException {

        List<RasTestClass> classArray = getTestClasses();

		classArray.sort(Comparator.comparing(RasTestClass::getTestClass));

        try {
			if(!sortQueryParameterChecker.isAscending("testclass")) {
				classArray.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
			}
		} catch (InternalServletException e) {
			ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED, "testclass");
			throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
		}

        /* converting data to json */
		JsonElement json = new Gson().toJsonTree(classArray);
		JsonObject testclasses = new JsonObject();
		testclasses.add("testclasses", json);
        return testclasses.toString();
    }
}
