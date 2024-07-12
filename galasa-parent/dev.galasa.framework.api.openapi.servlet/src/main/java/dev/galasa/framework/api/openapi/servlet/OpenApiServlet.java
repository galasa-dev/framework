/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.openapi.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.openapi.servlet.routes.OpenApiRoute;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
    "osgi.http.whiteboard.servlet.pattern=/openapi/*" }, name = "Galasa OpenAPI servlet")
public class OpenApiServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;

    protected Environment env = new SystemEnvironment();

	protected Log logger = LogFactory.getLog(this.getClass());

    @Override
	public void init() throws ServletException {
		super.init();

        try {
            String apiServerUrl = env.getenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
            addRoute(new OpenApiRoute(getResponseBuilder(), apiServerUrl));

            logger.info("OpenAPI Servlet Initialised");

        } catch (IOException ex) {
            throw new ServletException("Failed to initialise OpenAPI servlet", ex);
        }

	}
}