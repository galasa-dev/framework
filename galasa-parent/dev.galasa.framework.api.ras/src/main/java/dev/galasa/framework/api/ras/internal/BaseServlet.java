/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.ras.internal.commons.InternalServletException;
import dev.galasa.framework.api.ras.internal.commons.QueryParameters;

import dev.galasa.framework.api.ras.internal.commons.ServletError;
import dev.galasa.framework.api.ras.internal.routes.IRoute;
import dev.galasa.framework.api.ras.internal.routes.RunArtifactsDownloadRoute;
import dev.galasa.framework.api.ras.internal.routes.RunArtifactsListRoute;
import dev.galasa.framework.api.ras.internal.routes.RunDetailsRoute;
import dev.galasa.framework.api.ras.internal.routes.RunLogRoute;
import dev.galasa.framework.api.ras.internal.routes.RunQueryRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.ras.internal.commons.ServletErrorMessage.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Proxy Servlet for the /ras/* endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/*" }, name = "Galasa Ras microservice")
public class BaseServlet extends HttpServlet {

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

	private Log  logger  =  LogFactory.getLog(this.getClass());

	protected IFileSystem fileSystem = new FileSystem();

	static final Gson gson = GalasaGsonBuilder.build();
	
	private final Map<String, IRoute> routes = new HashMap<>();
 
	
	@Override
	public void init() {
	   addRoute(new RunDetailsRoute(framework));
	   addRoute(new RunLogRoute(framework));
	   addRoute(new RunArtifactsListRoute(fileSystem, framework));
	   addRoute(new RunQueryRoute(framework));
	   addRoute(new RunArtifactsDownloadRoute(fileSystem, framework));
	}
 
	private void addRoute(IRoute route) {
	   routes.put(route.getPath(), route);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
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
						res = route.handleRequest(url, queryParameters, res);
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
			res = sendResponse(res, response, httpStatusCode);
		}
	}

	public static HttpServletResponse sendResponse(HttpServletResponse resp , String json , int status){
		//Set headers for HTTP Response
		resp.setStatus(status);
		resp.setContentType( "Application/json");
		resp.addHeader("Access-Control-Allow-Origin", "*");
		try {
			PrintWriter out = resp.getWriter();
			out.print(json);
			out.close();
		} catch (Exception e) {
		}
		return resp;
	}
}
