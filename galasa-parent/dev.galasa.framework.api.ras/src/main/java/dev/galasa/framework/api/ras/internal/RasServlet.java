/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.common.BaseServlet;

import dev.galasa.framework.api.ras.internal.routes.ResultNamesRoute;
import dev.galasa.framework.api.ras.internal.routes.RunArtifactsDownloadRoute;
import dev.galasa.framework.api.ras.internal.routes.RunArtifactsListRoute;
import dev.galasa.framework.api.ras.internal.routes.RunDetailsRoute;
import dev.galasa.framework.api.ras.internal.routes.RunLogRoute;
import dev.galasa.framework.api.ras.internal.routes.RunQueryRoute;


import javax.servlet.Servlet;


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
