/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.ras.internal.routes.ResultNamesRoute;
import dev.galasa.framework.api.ras.internal.routes.RunArtifactsDownloadRoute;
import dev.galasa.framework.api.ras.internal.routes.RunArtifactsListRoute;
import dev.galasa.framework.api.ras.internal.routes.RunDetailsRoute;
import dev.galasa.framework.api.ras.internal.routes.RunLogRoute;
import dev.galasa.framework.api.ras.internal.routes.RunQueryRoute;
import dev.galasa.framework.api.ras.internal.verycommon.BaseServlet;

import dev.galasa.framework.spi.IFramework;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

/*
 * Proxy Servlet for the /ras/* endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/*" }, name = "Galasa Ras microservice")
public class RasServlet extends BaseServlet {

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

	protected Log  logger  =  LogFactory.getLog(this.getClass());
	
	protected IFileSystem fileSystem = new FileSystem();
 
	@Override
	public void init() throws ServletException {
		logger.info("RasServlet initialising");

		super.init();
		
		addRoute(new RunDetailsRoute(getResponseBuilder(),framework));
	   	addRoute(new RunLogRoute(getResponseBuilder(),framework));
	   	addRoute(new RunArtifactsListRoute(getResponseBuilder(),fileSystem, framework));
	   	addRoute(new RunQueryRoute(getResponseBuilder(),framework));
	   	addRoute(new RunArtifactsDownloadRoute(getResponseBuilder(),fileSystem, framework));
	   	addRoute(new ResultNamesRoute(getResponseBuilder(),framework));
	}

}
