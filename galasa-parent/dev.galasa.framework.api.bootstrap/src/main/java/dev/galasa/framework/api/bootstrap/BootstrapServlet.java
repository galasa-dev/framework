/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.api.bootstrap.routes.BootstrapExternalRoute;
import dev.galasa.framework.api.bootstrap.routes.BootstrapInternalRoute;
/*
 * Proxy Servlet for the /bootstrap endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
	"osgi.http.whiteboard.servlet.pattern=/bootstrap" }, name = "Galasa Bootstrap")
public class BootstrapServlet extends BaseServlet {

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

	protected Log  logger  =  LogFactory.getLog(this.getClass());
	
	@Override
	public void init() throws ServletException {
		logger.info("Bootstrap Servlet initialising");

		super.init();

		addRoute(new BootstrapExternalRoute(getResponseBuilder()));
		addRoute(new BootstrapInternalRoute(getResponseBuilder(), framework));

	}

    
}
