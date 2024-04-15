/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
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
	"osgi.http.whiteboard.servlet.pattern=/bootstrap/*" }, name = "Galasa Bootstrap")
public class BootstrapServlet extends BaseServlet {

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

    private final Properties configurationProperties = new Properties();

    private final ArrayList<String> bootstrapKeys = new ArrayList<>(Arrays.asList("framework.config.store",
            "framework.extra.bundles", "framework.testcatalog.url"));

	protected Log logger = LogFactory.getLog(this.getClass());

	@Override
	public void init() throws ServletException {
		logger.info("Bootstrap Servlet initialising");

		super.init();

		addRoute(new BootstrapExternalRoute(getResponseBuilder()));
		addRoute(new BootstrapInternalRoute(getResponseBuilder(), configurationProperties));
	}

    @Activate
    public void activate(Map<String, Object> properties) {
        modified(properties);
        logger.info("Galasa Bootstrap API activated");
    }

    @Modified
    public void modified(Map<String, Object> properties) {
        synchronized (configurationProperties) {
            for (String key : bootstrapKeys) {
                String value = (String) properties.get(key);
                if (value != null) {
                    this.configurationProperties.put(key, value);
                } else {
                    this.configurationProperties.remove(key);
                }
            }
        }
    }

    @Deactivate
    public void deactivate() {
        synchronized (configurationProperties) {
            this.configurationProperties.clear();
        }
    }
}