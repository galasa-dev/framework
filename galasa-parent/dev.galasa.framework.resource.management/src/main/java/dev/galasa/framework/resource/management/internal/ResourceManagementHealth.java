/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.resource.management.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class ResourceManagementHealth implements HttpHandler {
    
    private Log                      logger = LogFactory.getLog(this.getClass());

    private final ResourceManagement resourceManagement;

    private final HttpServer         httpServer;

    public ResourceManagementHealth(ResourceManagement resourceManagement, int port) throws FrameworkException {
        this.resourceManagement = resourceManagement;

        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            this.httpServer.createContext("/", this);
            this.httpServer.start();
        } catch (IOException e) {
            throw new FrameworkException("Unable to initialise the health http server", e);
        }

    }

    public void shutdown() {
        this.httpServer.stop(0);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();
            return;
        }
        
        logger.trace("Health check request received");

        long successfulRuns = this.resourceManagement.getSuccessfulRunsSinceLastHealthCheck();

        String response = "successfulruns=" + Long.toString(successfulRuns);
        int responseCode = 200;
        if (successfulRuns == 0) {
            logger.error("Health check failed, there were no successful runs since last health check");
            responseCode = 500;
        }

        exchange.sendResponseHeaders(responseCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
