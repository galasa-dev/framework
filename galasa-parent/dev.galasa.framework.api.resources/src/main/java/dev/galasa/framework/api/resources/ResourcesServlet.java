/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.resources.routes.ResourcesRoute;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
/*
 * Proxy Servlet for the /resources/* endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/resources/*" }, name = "Galasa Resources microservice")
public class ResourcesServlet extends BaseServlet {

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

	protected Log  logger  =  LogFactory.getLog(this.getClass());

	protected IFileSystem fileSystem = new FileSystem();
	
	protected IFramework getFramework() {
        return this.framework;
    }

	protected void setFramework(IFramework framework) {
        this.framework = framework;
    }

	@Override
	public void init() throws ServletException {
		logger.info("Resources Servlet initialising");

		super.init();

		try {
            addRoute(new ResourcesRoute(getResponseBuilder(), framework));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ServletException("Failed to initialise Resources Servlet", e);
        }
        logger.info("Resources Servlet initialised");
	}

    
}
