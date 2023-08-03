/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.component.annotations.ConfigurationPolicy;

//@Component(service = Filter.class, scope = ServiceScope.PROTOTYPE, property = {
//        "osgi.http.whiteboard.filter.pattern=/*" }, configurationPid = {
//                "dev.galasa" }, configurationPolicy = ConfigurationPolicy.REQUIRE, name = "Galasa JWT Auth")
public class JwtAuthFilter implements Filter {

    private final Log     logger                  = LogFactory.getLog(getClass());
    private static String SECRET_KEY              = "framework.jwt.secret";

    private Properties    configurationProperties = new Properties();

    @Activate
    void activate(Map<String, Object> properties) {
        synchronized (configurationProperties) {
            String secret = (String) properties.get(SECRET_KEY);
            if (secret != null) {
                this.configurationProperties.put(SECRET_KEY, secret);
            } else {
                this.configurationProperties.remove(SECRET_KEY);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        if ("/auth".equals(servletRequest.getServletPath())) { // dont do this for the auth url
            chain.doFilter(request, response);
            return;
        }

        Principal principal = servletRequest.getUserPrincipal();
        if (principal != null) { // already authenticated
            chain.doFilter(request, response);
            return;
        }

        String authorization = servletRequest.getHeader("Authorization");
        if (authorization == null) {
            chain.doFilter(request, response);
            return;
        }

        StringTokenizer st = new StringTokenizer(authorization);
        if (!st.hasMoreTokens()) {
            chain.doFilter(request, response);
            return;
        }

        String bearer = st.nextToken();
        if (!"bearer".equalsIgnoreCase(bearer)) {
            chain.doFilter(request, response);
            return;
        }

        if (!st.hasMoreTokens()) {
            chain.doFilter(request, response);
            return;
        }

        String sJwt = st.nextToken();
        Algorithm algorithm = Algorithm.HMAC256(this.configurationProperties.getProperty(SECRET_KEY));

        JWTVerifier verifier = JWT.require(algorithm).withIssuer("galasa").build();
        try {
            DecodedJWT jwt = verifier.verify(sJwt);

            String subject = jwt.getSubject();
            String role = jwt.getClaim("role").asString();

            JwtRequestWrapper wrapper = new JwtRequestWrapper(subject, role, servletRequest);

            chain.doFilter(wrapper, servletResponse);
            return;
        } catch (AlgorithmMismatchException e) {
            chain.doFilter(request, response);
            invalidAuth(servletRequest, servletResponse, "Incorrect Algorithim " + e);
            return;
        } catch (SignatureVerificationException e) {
            chain.doFilter(request, response);
            invalidAuth(servletRequest, servletResponse, "Non valid signature " + e);
        } catch (TokenExpiredException e) {
            chain.doFilter(request, response);
            invalidAuth(servletRequest, servletResponse, "Jwt has expired " + e);
        } catch (InvalidClaimException e) {
            chain.doFilter(request, response);
            invalidAuth(servletRequest, servletResponse, "Invalid Claims " + e);
        }
        // chain.doFilter(servletRequest, servletResponse);
    }

    private void invalidAuth(HttpServletRequest servletRequest, HttpServletResponse servletResponse, String jwtResponse)
            throws IOException {
        servletResponse.setContentType("text/plain");
        servletResponse.addHeader("WWW-Authenticate", "Bearer realm=\"Galasa\""); // *** Ability to set the realm
        servletResponse.getWriter().write(jwtResponse);
        return;
    }

    @Override
    public void destroy() {
    }

}
