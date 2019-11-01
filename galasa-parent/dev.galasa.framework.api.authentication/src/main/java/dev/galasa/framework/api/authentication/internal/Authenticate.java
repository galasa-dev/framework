/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Map;
import java.util.Properties;


import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.gson.Gson;

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
		property= {"osgi.http.whiteboard.servlet.pattern=/auth"},
		configurationPid= {"dev.galasa"},
		configurationPolicy=ConfigurationPolicy.REQUIRE,
		name="Galasa Authentication"
		)
public class Authenticate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String SECRET_KEY = "framework.jwt.secret";
	private static long FOUR_HOURS_EXPIRE = 14400000;
	
	@Reference
	public IFramework framework;   // NOSONAR

	private final Properties configurationProperties = new Properties();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Gson gson = new Gson();
		Principal principal = req.getUserPrincipal();

		if (principal == null) { // TODO check that it was a basic auth principal to prevent JWT reauthenticating
			resp.setStatus(401);
			resp.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\"");  //*** Ability to set the realm
			resp.getWriter().write("Requires authentication");//NOSONAR  //TODO catch this as SQ says
			return;
		}
		if (req.isUserInRole("admin")){ 
			String jwt;
			try {
				jwt = createJWT(principal.getName(), "admin", FOUR_HOURS_EXPIRE);
			} catch (JWTCreationException e) {
				resp.setStatus(500);
				resp.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\"");  //*** Ability to set the realm
				resp.getWriter().write("Token could not be generated");//NOSONAR  //TODO catch this as SQ says
				return;
			}

			AuthJson auth = new AuthJson();
			auth.cps = jwt;
			auth.dss = jwt;
			auth.ras = jwt;
			String json = gson.toJson(auth);

			resp.setContentType("application/json");
			try{
				resp.getWriter().write(json);
			} catch (IOException e) {
				resp.setStatus(500);
				resp.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\"");  //*** Ability to set the realm
				resp.getWriter().write("Failed to create json");//NOSONAR  //TODO catch this as SQ says
				return;
			}
			return;
		} 
		if (req.isUserInRole("user")) {
			String jwt;
			try {
				jwt = createJWT(principal.getName(), "user", FOUR_HOURS_EXPIRE);
			} catch (JWTCreationException e) {
				resp.setStatus(500);
				resp.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\"");  //*** Ability to set the realm
				resp.getWriter().write("Token could not be generated");//NOSONAR  //TODO catch this as SQ says
				return;
			}

			AuthJson auth = new AuthJson();
			auth.cps = jwt;
			auth.dss = jwt;
			auth.ras = jwt;

			String json = gson.toJson(auth);

			resp.setContentType("application/json");
			try{
				resp.getWriter().write(json);
			} catch (IOException e) {
				resp.setStatus(500);
				resp.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\"");  //*** Ability to set the realm
				resp.getWriter().write("Failed to create json");//NOSONAR  //TODO catch this as SQ says
				return;
			}
			return;
		} 

		resp.setStatus(401);
		resp.addHeader("WWW-Authenticate", "Basic realm=\"Galasa\"");  //*** Ability to set the realm
		resp.getWriter().write("Does not have the 'user' role");//NOSONAR
	}
	
	public String createJWT(String subject, String role, long expireDuration) throws JWTCreationException {
		Algorithm algorithm = Algorithm.HMAC256(this.configurationProperties.get(SECRET_KEY).toString());
		
		long time = System.currentTimeMillis();
		Date dateNow = new Date(time);
		Date dateExpire = new Date(time+expireDuration);
		
		String token = JWT.create()
						.withIssuer("galasa")
						.withIssuedAt(dateNow)
						.withSubject(subject)
						.withClaim("role", role)
						.withExpiresAt(dateExpire)
						.sign(algorithm);
						
		return token;
	}

	@Activate
	public void activate(Map<String, Object> properties) {
		modified(properties);
	}

	@Modified
	public void modified(Map<String, Object> properties) {
		synchronized (configurationProperties) {
			String secret = (String)properties.get(SECRET_KEY);
			if (secret != null) {
				this.configurationProperties.put(SECRET_KEY, secret);
			} else {
				this.configurationProperties.remove(SECRET_KEY);
			}
		}
	}
	
	@Deactivate
	void deactivate() {
		this.configurationProperties.clear();
	}
	private class AuthJson {
		protected String cps;
		protected String dss;
		protected String ras;
	}
}
