/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.common;

import org.osgi.service.component.annotations.Reference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

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

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

	protected Log  logger  =  LogFactory.getLog(this.getClass());

	static final Gson gson = GalasaGsonBuilder.build();
	
	private final Map<String, IRoute> routes = new HashMap<>();

	private ResponseBuilder responseBuilder = new ResponseBuilder();
	
	protected void addRoute(IRoute route) {
	   routes.put(route.getPath(), route);
	}

	protected ResponseBuilder getResponseBuilder() {
		return this.responseBuilder;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String url = req.getPathInfo();
		String response = "";
		int httpStatusCode = HttpServletResponse.SC_OK;
		QueryParameters queryParameters = new QueryParameters(req.getParameterMap());
		try {
			if (url != null) {
				for (Map.Entry<String, IRoute> entry : routes.entrySet()) {
		
					String routePattern = entry.getKey();
					IRoute route = entry.getValue();
					
					Matcher matcher = Pattern.compile(routePattern).matcher(url);
		
					if (matcher.matches()) {		
						res = route.handleGetRequest(url, queryParameters, res);
						return;
					}
				}

				// No matching route was found, throw a 404 error.
				ServletError error = new ServletError(GAL5404_UNRESOLVED_ENDPOINT_ERROR, url);
				throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (InternalServletException ex) {
			// the message is a curated servlet message, we intentionally threw up to this level.
		   	response = ex.getMessage();
			httpStatusCode = ex.getHttpFailureCode();
			logger.error(response, ex);
	   	} catch (Throwable t) {
			// We didn't expect this failure to arrive. So deliver a generic error message.
			response = new ServletError(GAL5000_GENERIC_API_ERROR).toString();
			httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			logger.error(response,t);
		}

		if (!response.isEmpty()) {
			res = getResponseBuilder().sendResponse(res, "application/json", response, httpStatusCode);
		}
	}

	
}
