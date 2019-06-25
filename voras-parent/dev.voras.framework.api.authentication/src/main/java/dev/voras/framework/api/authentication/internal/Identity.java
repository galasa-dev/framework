package dev.voras.framework.api.authentication.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import dev.voras.framework.spi.IFramework;

import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

@Component(
    service=Servlet.class,
    scope=ServiceScope.PROTOTYPE,
    configurationPid= {"dev.voras"},
	configurationPolicy=ConfigurationPolicy.REQUIRE,
    property=("osgi.http.whiteboard.servlet.pattern=/auth/identity"),
	name="Voras Identity"
)
public class Identity extends HttpServlet {
    private Properties configurationProperties = new Properties();
    private static String SECRET_KEY = "framework.jwt.secret";

    @Reference
    public IFramework framework;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        Gson gson = new Gson();        

        String jwt = getBearerToken(request);
        String subject = JWT.decode(jwt).getSubject();



        if(request.isUserInRole("admin")) {
            response.getWriter().write(subject + " is in admin\n");
            return;
        }
        if(request.isUserInRole("user")) {
            response.getWriter().write(subject + " is in user\n");
            return;
        }

        // response.setStatus(401);
		response.addHeader("WWW-Authenticate", "Basic realm=\"Voras\"");  //*** Ability to set the realm
		response.getWriter().write("No authetication!\n");//NOSONAR
        return;
    }

    @Activate
	void activate(Map<String, Object> properties) {
		synchronized (configurationProperties) {
			String secret = (String)properties.get(SECRET_KEY);
			if (secret != null) {
				this.configurationProperties.put(SECRET_KEY, secret);
			} else {
				this.configurationProperties.remove(SECRET_KEY);
			}
		}
	}

    private String getBearerToken( HttpServletRequest request ) {
        String authHeader = request.getHeader( "Authorization" );
        if ( authHeader != null && authHeader.startsWith( "Bearer" ) ) {
            return authHeader.substring( "bearer".length() );
        }
        return null;
	}
}