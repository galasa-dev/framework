/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import java.io.IOException;
import java.net.http.HttpClient;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SystemEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

@Component(service = Filter.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.filter.pattern=/*" }, name = "Galasa JWT Auth")
public class JwtAuthFilter implements Filter {

    private final Log logger = LogFactory.getLog(getClass());

    private ResponseBuilder responseBuilder = new ResponseBuilder();

    protected Environment env = new SystemEnvironment();

    protected OidcProvider oidcProvider;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        oidcProvider = new OidcProvider(env.getenv(EnvironmentVariables.GALASA_DEX_ISSUER), HttpClient.newHttpClient());
        logger.info("Galasa JWT Auth Filter initialised");
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

        // Do not apply the filter to the /auth endpoint and only force galasactl to authenticate
        if ((servletRequest.getServletPath().equals("/auth") && servletRequest.getPathInfo() == null)
                || !"galasactl".equalsIgnoreCase(servletRequest.getHeader("Galasa-Application"))) {
            chain.doFilter(request, response);
            return;
        }

        String errorString = "";
        int httpStatusCode = HttpServletResponse.SC_OK;

        try {
            String sJwt = JwtWrapper.getBearerTokenFromAuthHeader(servletRequest);
            if (sJwt != null) {

                // Only allow the request through the filter if the provided JWT is valid
                if (oidcProvider.isJwtValid(sJwt)) {
                    chain.doFilter(request, response);
                    return;
                }
            }

            // Throw an unauthorized exception
            ServletError error = new ServletError(GAL5401_UNAUTHORIZED);
            throw new InternalServletException(error, HttpServletResponse.SC_UNAUTHORIZED);

        } catch (InternalServletException e) {
            errorString = e.getMessage();
            httpStatusCode = e.getHttpFailureCode();
        } catch (Exception e) {
            errorString = new ServletError(GAL5000_GENERIC_API_ERROR).toString();
            httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        responseBuilder.buildResponse(servletResponse, "application/json", errorString, httpStatusCode);
    }

    @Override
    public void destroy() {
    }

}