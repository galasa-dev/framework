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
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.ras.internal.common.RasQueryParameters;
import dev.galasa.framework.api.ras.internal.verycommon.*;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class TestClassesRoute extends RunsRoute {

    public TestClassesRoute(ResponseBuilder responseBuilder, String path, IFramework framework) {
        /* Regex to match endpoints: 
		*  -> /ras/testclasses
		*  -> /ras/testclasses?
		*/
        super(responseBuilder, "\\/testclasses?", framework);
    }

    final static Gson gson = GalasaGsonBuilder.build();

    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response)
    throws ServletException, IOException, FrameworkException {
        String outputString = retrieveTestClasses(new RasQueryParameters(queryParams));
        return getResponseBuilder().buildResponse(response, "application/json", outputString, HttpServletResponse.SC_OK); 
    }
    
    private String retrieveTestClasses (RasQueryParameters params) throws ResultArchiveStoreException, InternalServletException, ServletException {
        List<RasTestClass> testClassesArray = getTestClasses();

		testClassesArray.sort(Comparator.comparing(RasTestClass::getTestClass));
        if (params.getSortValue() !=null){
			if(!params.isAscending("testclass")) {
				testClassesArray.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
			}
        }

        /* converting data to json */
		JsonElement json = new Gson().toJsonTree(testClassesArray);
		JsonObject testclasses = new JsonObject();
		testclasses.add("testclasses", json);
        return testclasses.toString();
    }
}
