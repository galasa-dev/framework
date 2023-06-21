package dev.galasa.framework.api.ras.internal.routes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.framework.api.ras.internal.common.InternalServletException;
import dev.galasa.framework.api.ras.internal.common.QueryParameters;
import dev.galasa.framework.api.ras.internal.common.ServletError;
import dev.galasa.framework.api.ras.internal.common.SortQueryParameterChecker;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.BaseServlet.*;
import static dev.galasa.framework.api.ras.internal.common.ServletErrorMessage.*;

public class ResultNamesRoute extends RunsRoute {

	public ResultNamesRoute(IFramework framework) {
		/* Regex to match endpoints: 
		*  -> /ras/runs
		*  -> /ras/runs/
		*  -> /ras/runs?{querystring} 
		*/
		super("\\/resultnames?");
		this.framework = framework;
	}

	final static Gson gson = GalasaGsonBuilder.build();
    private SortQueryParameterChecker sortQueryParameterChecker = new SortQueryParameterChecker();

    @Override
    public HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) throws ServletException, IOException, FrameworkException {
        String outputString = retrieveResults(queryParams);
		return sendResponse(response, "application/json", outputString, HttpServletResponse.SC_OK); 
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

		String json = gson.toJson(resultsList);
		return json;
    }

}
