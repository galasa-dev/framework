package dev.voras.framework.api.bootstrap.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.voras.framework.spi.IFramework;

/**
 * Bootstrap servlet - list the basic CPS properties 
 * 
 * Must not require authentication 
 * 
 * @author Michael Baylis
 *
 */
@Component(
		service=Servlet.class,
		scope=ServiceScope.PROTOTYPE,
		property= {"osgi.http.whiteboard.servlet.pattern=/bootstrap"},
		configurationPid= {"dev.voras"},
		configurationPolicy=ConfigurationPolicy.REQUIRE,
		name="Voras Bootstrap"
		)
public class Bootstrap extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String CPS   = "framework.config.store";
	private static final String EXTRA = "framework.extra.bundles";

	@Reference
	public IFramework framework;   // NOSONAR

	private final Properties configurationProperties = new Properties();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Properties actualBootstrap = new Properties();
		synchronized (this.configurationProperties) {
			actualBootstrap.putAll(this.configurationProperties);
		}

		if (this.framework == null || this.framework.isInitialised()) {
			//TODO look for additional bootstrap properties like the auth server
		}
		
		resp.setStatus(200);
		resp.setContentType("text/plain");
		actualBootstrap.store(resp.getWriter(), "Voras Bootstrap Properties");
	}


	@Activate
	void activate(Map<String, Object> properties) {
		modified(properties);
	}

	@Modified
	void modified(Map<String, Object> properties) {
		synchronized (configurationProperties) {
			String cps = (String) properties.get(CPS);
			if (cps != null) {
				this.configurationProperties.put(CPS, cps);
			} else {
				this.configurationProperties.remove(CPS);
			}
			
			String extra = (String) properties.get(EXTRA);
			if (extra != null) {
				this.configurationProperties.put(EXTRA, extra);
			} else {
				this.configurationProperties.remove(EXTRA);
			}
		}
	}

	@Deactivate
	void deactivate() {
		synchronized (configurationProperties) {
			this.configurationProperties.remove(EXTRA);
			this.configurationProperties.remove(EXTRA);
		}    
	}

}
