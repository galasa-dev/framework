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
import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.ras.internal.routes.ResultNamesRoute;
import dev.galasa.framework.api.ras.internal.routes.RunArtifactsDownloadRoute;
import dev.galasa.framework.api.ras.internal.routes.RunArtifactsListRoute;
import dev.galasa.framework.api.ras.internal.routes.RunDetailsRoute;
import dev.galasa.framework.api.ras.internal.routes.RunLogRoute;
import dev.galasa.framework.api.ras.internal.routes.RunQueryRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

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
public class RasServlet extends BaseServlet {
	
	private static final long serialVersionUID = 1L;

	protected IFileSystem fileSystem = new FileSystem();
 
	@Override
	public void init() {
	   super.addRoute(new RunDetailsRoute(getResponseBuilder(),framework));
	   super.addRoute(new RunLogRoute(getResponseBuilder(),framework));
	   super.addRoute(new RunArtifactsListRoute(getResponseBuilder(),fileSystem, framework));
	   super.addRoute(new RunQueryRoute(getResponseBuilder(),framework));
	   super.addRoute(new RunArtifactsDownloadRoute(getResponseBuilder(),fileSystem, framework));
	   super.addRoute(new ResultNamesRoute(getResponseBuilder(),framework));
	}

}
