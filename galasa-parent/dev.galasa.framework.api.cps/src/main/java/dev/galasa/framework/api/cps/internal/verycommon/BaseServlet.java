/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.verycommon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;


import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.cps.internal.verycommon.ServletErrorMessage.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Proxy Servlet for the /ras/* endpoints
 */
public class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected Log  logger  =  LogFactory.getLog(this.getClass());

	static final Gson gson = GalasaGsonBuilder.build();
	
	private final Map<String, IRoute> routes = new HashMap<>();

	private ResponseBuilder responseBuilder = new ResponseBuilder();
	
	protected void addRoute(IRoute route) {
		String path = route.getPath();
		logger.info("Base servlet adding route "+path);
	   	routes.put(path, route);
	}

	protected ResponseBuilder getResponseBuilder() {
		return this.responseBuilder;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		String url = req.getPathInfo();
		
		logger.info("BaseServlet : doGet() entered. Url:"+url);
		
		String errorString = "";
		int httpStatusCode = HttpServletResponse.SC_OK;
		QueryParameters queryParameters = new QueryParameters(req.getParameterMap());

		logger.info("BaseServlet : doGet() query parameters extracted.");
		try {
			if (url != null) {
				for (Map.Entry<String, IRoute> entry : routes.entrySet()) {
		
					String routePattern = entry.getKey();
					IRoute route = entry.getValue();
					
					Matcher matcher = Pattern.compile(routePattern).matcher(url);
		
					if (matcher.matches()) {	
						logger.info("BaseServlet : doGet() Found a route that matches.");	
						route.handleRequest(url, queryParameters, res);
						return;
					}
				}

				// No matching route was found, throw a 404 error.
				logger.info("BaseServlet : doGet() no a route that matches.");
				ServletError error = new ServletError(GAL5404_UNRESOLVED_ENDPOINT_ERROR, url);
				throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (InternalServletException ex) {
			// the message is a curated servlet message, we intentionally threw up to this level.
		   	errorString = ex.getMessage();
			httpStatusCode = ex.getHttpFailureCode();
			logger.error(errorString, ex);
	   	} catch (Throwable t) {
			// We didn't expect this failure to arrive. So deliver a generic error message.
			errorString = new ServletError(GAL5000_GENERIC_API_ERROR).toString();
			httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			logger.error(errorString,t);
		}

		getResponseBuilder().buildResponse(res, "application/json", errorString, httpStatusCode);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

   }

   @Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String url = req.getPathInfo();
		
		logger.info("BaseServlet : doPut() entered. Url:"+url);
		
		String errorString = "";
		int httpStatusCode = HttpServletResponse.SC_CREATED;
		QueryParameters queryParameters = new QueryParameters(req.getParameterMap());

		logger.info("BaseServlet : doPut() query parameters extracted.");
		try {
			if (url != null) {
				for (Map.Entry<String, IRoute> entry : routes.entrySet()) {
		
					String routePattern = entry.getKey();
					IRoute route = entry.getValue();
					
					Matcher matcher = Pattern.compile(routePattern).matcher(url);
		
					if (matcher.matches()) {	
						logger.info("BaseServlet : doPut() Found a route that matches.");	
						route.handlePutRequest(url, queryParameters, req, res);
						return;
					}
				}

				// No matching route was found, throw a 404 error.
				logger.info("BaseServlet : doGet() no a route that matches.");
				ServletError error = new ServletError(GAL5404_UNRESOLVED_ENDPOINT_ERROR, url);
				throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (InternalServletException ex) {
			// the message is a curated servlet message, we intentionally threw up to this level.
		   	errorString = ex.getMessage();
			httpStatusCode = ex.getHttpFailureCode();
			logger.error(errorString, ex);
	   	} catch (Throwable t) {
			// We didn't expect this failure to arrive. So deliver a generic error message.
			errorString = new ServletError(GAL5000_GENERIC_API_ERROR).toString();
			httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			logger.error(errorString,t);
		}

		getResponseBuilder().buildResponse(res, "application/json", errorString, httpStatusCode);
	}

   @Override
   protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

   }

}

