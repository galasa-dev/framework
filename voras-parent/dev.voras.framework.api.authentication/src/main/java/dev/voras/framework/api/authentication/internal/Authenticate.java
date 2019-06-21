package dev.voras.framework.api.authentication.internal;

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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.voras.framework.spi.IFramework;

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
		configurationPid= {"dev.voras"},
		configurationPolicy=ConfigurationPolicy.REQUIRE,
		name="Voras Authentication"
		)
public class Authenticate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String SECRET_KEY = "thisIsthineKey";
	
	@Reference
	public IFramework framework;   // NOSONAR

	private final Properties configurationProperties = new Properties();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Principal principal = req.getUserPrincipal();
		if (principal == null) { // TODO check that it was a basic auth principal to prevent JWT reauthenticating
			resp.setStatus(401);
			resp.addHeader("WWW-Authenticate", "Basic realm=\"Voras\"");  //*** Ability to set the realm
			resp.getWriter().write("Requires authentication");//NOSONAR  //TODO catch this as SQ says
			return;
		}
		
		if (!req.isUserInRole("user")) {
			resp.setStatus(401);
			resp.addHeader("WWW-Authenticate", "Basic realm=\"Voras\"");  //*** Ability to set the realm
			resp.getWriter().write("Does not have the 'user' role");//NOSONAR
			return;
		}
		
		if (req.isUserInRole("admin")) {
			//Create admin jwt
			String test = principal.toString();
		} else {
			//create user jwt
		}
		// TODO create and return the JWT		
		
		resp.setStatus(503);
		resp.setContentType("text/plain");
		resp.getWriter().write("James hasn't written the code yet");//NOSONAR
	}

	public static String createJWT(String subject, String role, long expireDuration) throws JWTCreationException {
		Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

		long time = System.currentTimeMillis();
		Date dateNow = new Date(time);
		Date dateExpire = new Date(time+expireDuration);

		String token = JWT.create()
						.withIssuer("voras")
						.withIssuedAt(dateNow)
						.withSubject(subject)
						.withClaim("role", role)
						.withExpiresAt(dateExpire)
						.sign(algorithm);
						
		return token;
	}

	@Activate
	void activate(Map<String, Object> properties) {
		modified(properties);
	}

	@Modified
	void modified(Map<String, Object> properties) {

	}
	
	@Deactivate
	void deactivate() {
		//TODO Clear the properties to prevent JWT generation
	}

}
