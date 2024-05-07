/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import java.net.http.HttpClient;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.internal.routes.AuthCallbackRoute;
import dev.galasa.framework.api.authentication.internal.routes.AuthClientsRoute;
import dev.galasa.framework.api.authentication.internal.routes.AuthRoute;
import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.SystemEnvironment;

/**
 * Authentication Servlet that acts as a proxy to send requests to Dex's /token
 * endpoint, returning the JWT received back from Dex.
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/auth/*" }, name = "Galasa Authentication")
public class AuthenticationServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    protected Environment env = new SystemEnvironment();
    protected IOidcProvider oidcProvider;
    protected DexGrpcClient dexGrpcClient;

    @Override
    public void init() throws ServletException {
        logger.info("Galasa Authentication API initialising");

        // Make sure the relevant environment variables have been set, otherwise the servlet won't be able to talk to Dex
        String externalApiServerUrl = getRequiredEnvVariable(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
        String dexIssuerUrl = getRequiredEnvVariable(EnvironmentVariables.GALASA_DEX_ISSUER);
        String dexGrpcHostname = getRequiredEnvVariable(EnvironmentVariables.GALASA_DEX_GRPC_HOSTNAME);

        initialiseDexClients(dexIssuerUrl, dexGrpcHostname);

        addRoute(new AuthRoute(getResponseBuilder(), oidcProvider, dexGrpcClient));
        addRoute(new AuthClientsRoute(getResponseBuilder(), dexGrpcClient));
        addRoute(new AuthCallbackRoute(getResponseBuilder(), externalApiServerUrl));

        logger.info("Galasa Authentication API initialised");
    }

    /**
     * Initialises the OpenID Connect Provider and Dex gRPC client fields to allow
     * the authentication servlet to communicate with Dex.
     */
    protected void initialiseDexClients(String dexIssuerUrl, String dexGrpcHostname) {
        this.oidcProvider = new OidcProvider(dexIssuerUrl, HttpClient.newHttpClient());
        this.dexGrpcClient = new DexGrpcClient(dexGrpcHostname);
    }

    /**
     * Gets a given required environment variable, throwing a ServletException if a value has not been set.
     */
    private String getRequiredEnvVariable(String envName) throws ServletException {
        String envValue = env.getenv(envName);

        if (envValue == null) {
            throw new ServletException("Required environment variable '" + envName + "' has not been set.");
        }
        return envValue;
    }
}