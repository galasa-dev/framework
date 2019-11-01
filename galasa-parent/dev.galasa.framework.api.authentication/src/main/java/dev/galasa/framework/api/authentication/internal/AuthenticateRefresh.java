/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.IOException;
import java.security.Principal;
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

import dev.galasa.framework.spi.IFramework;

/**
 * Authentication JWT generator
 * 
 * For the JWT to be created, the user needs to be authenticated by the Servlet Filters
 * 
 */
@Component(
		service=Servlet.class,
		scope=ServiceScope.PROTOTYPE,
		property= {"osgi.http.whiteboard.servlet.pattern=/authrefresh"},
		configurationPid= {"dev.galasa"},
		configurationPolicy=ConfigurationPolicy.REQUIRE,
		name="Galasa Authentication Refresh"
		)
public class AuthenticateRefresh extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Reference
	public IFramework framework;  //NOSONAR

	private final Properties configurationProperties = new Properties();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Principal principal = req.getUserPrincipal();
		if (principal == null) { // TODO check that it was a JWT auth principal
			resp.setStatus(401);
			resp.addHeader("WWW-Authenticate", "Bearer realm=\"Galasa\"");  //*** Ability to set the realm
			resp.getWriter().write("Requires JWT Token");//NOSONAR //TODO catch this as SQ says
			return;
		}
		
		if (!req.isUserInRole("user")) {
			resp.setStatus(401);
			resp.addHeader("WWW-Authenticate", "Bearer realm=\"Galasa\"");  //*** Ability to set the realm
			resp.getWriter().write("Does not have the 'user' role");//NOSONAR
			return;
		}
		
		// TODO create and return the refreshed JWT		
		
		resp.setStatus(503);
		resp.setContentType("text/plain");
		resp.getWriter().write("James hasn't written the code yet");//NOSONAR
	}


	@Activate
	void activate(Map<String, Object> properties) {
		modified(properties);
	}

	@Modified
	void modified(Map<String, Object> properties) {
		// TODO set the JWT signing key etc
	}

	@Deactivate
	void deactivate() {
		//TODO Clear the properties to prevent JWT generation
	}

}
