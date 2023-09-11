/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.cps.internal.routes.NamespacesRoute;
import dev.galasa.framework.api.cps.internal.verycommon.BaseServlet;

import dev.galasa.framework.spi.IFramework;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

/*
 * Proxy Servlet for the /ras/* endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/cps*" }, name = "Galasa CPS microservice")
public class CpsServlet extends BaseServlet {

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

	protected Log  logger  =  LogFactory.getLog(this.getClass());
	
	protected IFileSystem fileSystem = new FileSystem();
 
	@Override
	public void init() throws ServletException {
		logger.info("CPS Servlet initialising");

		super.init();
		
		addRoute(new NamespacesRoute(getResponseBuilder(),framework));

	}

}
