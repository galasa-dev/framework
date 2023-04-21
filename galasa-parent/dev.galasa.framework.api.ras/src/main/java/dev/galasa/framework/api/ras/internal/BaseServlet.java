/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.*;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaBundle;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaResult;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRunName;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;



public abstract class BaseServlet extends HttpServlet {

	@Reference
	IFramework framework;


	private static final long serialVersionUID = 1L;

	final static Gson gson = GalasaGsonBuilder.build();

	private Log  logger  =  LogFactory.getLog(this.getClass());


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try{
			Map<String,String[]> paramMap = req.getParameterMap();

			String responseBodyJson = retrieveResults(paramMap);

			sendResponse(resp, responseBodyJson, HttpServletResponse.SC_OK);

		} catch (InternalServletException ex ) {
			// the message is a curated servlet message, we intentionally threw up to this level.
			String responseBody = ex.getError().toString();
			int httpFailureCode = ex.getHttpFailureCode();
			sendResponse(resp, responseBody, httpFailureCode);
			logger.error(responseBody,ex);

		} catch (Throwable t){
			// We didn't expect this failure to arrive. So deliver a generic error message.
			String responseBody = new ServletError(GAL5000_GENERIC_API_ERROR).toString();
			int httpFailureCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			sendResponse(resp, responseBody, httpFailureCode);
			logger.error(responseBody,t);
		}
	};

    protected abstract String retrieveResults( 
		Map<String,String[]> rawParamMap
	) throws InternalServletException;

	protected void sendResponse(HttpServletResponse resp , String json , int status){
		//Set headers for HTTP Response
		resp.setStatus(status);
		resp.setContentType( "Application/json");
		resp.addHeader("Access-Control-Allow-Origin", "*");
		try{
			PrintWriter out = resp.getWriter();
			out.print(json);
			out.close();
		}catch(Exception e){
			logger.error("Error trying to set output buffer. Ignoring.",e);
		}
	}

	// TODO: Needs moving into a QueryParameterList, where it can be shared with RunQuery
    protected String extractSingleStringProperty(Map<String, String[]> paramMap, String key, String defaultValue ) throws InternalServletException {
		String returnedValue = defaultValue ;
		String[] paramValues = paramMap.get(key);
		if (paramValues != null && paramValues.length >0) {

			if (paramValues.length >1) {
				ServletError error = new ServletError(GAL5006_INVALID_QUERY_PARAM_DUPLICATES,key);
				throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
			}

			String firstOccurrance = paramValues[0];
			if( firstOccurrance != null ) {
				String trimmedFirstOccurrance = firstOccurrance.trim();
				if ( !(trimmedFirstOccurrance.isEmpty())) {
					returnedValue = trimmedFirstOccurrance;
				}
			}
		}
		return returnedValue;
	}
}