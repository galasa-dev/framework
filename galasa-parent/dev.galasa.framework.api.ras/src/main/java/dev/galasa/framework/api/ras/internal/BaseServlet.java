/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static dev.galasa.framework.api.ras.internal.ServletErrorMessage.*;

import com.google.gson.Gson;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

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
 
	private RunResultRas runResultRas;
	private RunLogRas runLogRas;
	
	@Override
	public void init() {
	   addRoute(new RunDetailsRoute(runResultRas));
	   addRoute(new RunLogRoute(runLogRas));
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
	
	@Activate
	public void activate() {
	   this.runResultRas = new RunResultRas(this.framework);
	   this.runLogRas = new RunLogRas(this.framework);
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
