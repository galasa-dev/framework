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
    protected OidcProvider oidcProvider;
    protected DexGrpcClient dexGrpcClient;

    @Override
    public void init() throws ServletException {
        logger.info("Galasa Authentication API initialising");

        initialiseDexClients();

        String externalApiServerUrl = env.getenv("GALASA_EXTERNAL_API_URL");

        addRoute(new AuthRoute(getResponseBuilder(), getServletInfo(), oidcProvider));
        addRoute(new AuthClientsRoute(getResponseBuilder(), getServletInfo(), dexGrpcClient));
        addRoute(new AuthCallbackRoute(getResponseBuilder(), getServletInfo(), externalApiServerUrl));

        logger.info("Galasa Authentication API initialised");
    }

    /**
     * Initialises the OpenID Connect Provider and Dex gRPC client fields to allow
     * the authentication servlet to communicate with Dex.
     */
    protected void initialiseDexClients() {
        this.oidcProvider = new OidcProvider(env.getenv("GALASA_DEX_ISSUER"), HttpClient.newHttpClient());
        this.dexGrpcClient = new DexGrpcClient(env.getenv("GALASA_DEX_GRPC_HOSTNAME"));
    }
}