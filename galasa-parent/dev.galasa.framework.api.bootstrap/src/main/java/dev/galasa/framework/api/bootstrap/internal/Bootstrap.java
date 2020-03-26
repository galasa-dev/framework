/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.bootstrap.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.IFramework;

/**
 * Bootstrap servlet - list the basic CPS properties
 * 
 * Must not require authentication
 * 
 * @author Michael Baylis
 *
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/bootstrap" }, configurationPid = {
                "dev.galasa.bootstrap" }, configurationPolicy = ConfigurationPolicy.REQUIRE, name = "Galasa Bootstrap")
public class Bootstrap extends HttpServlet {
    private static final long       serialVersionUID        = 1L;
    
    private Log logger = LogFactory.getLog(getClass());

    @Reference
    public IFramework               framework;                                                                       // NOSONAR

    private final Properties        configurationProperties = new Properties();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Properties actualBootstrap = new Properties();
        synchronized (this.configurationProperties) {
            actualBootstrap.putAll(this.configurationProperties);
        }

        if (this.framework != null && this.framework.isInitialised()) {
            // TODO look for additional bootstrap properties like the auth server
        }

        resp.setStatus(200);
        resp.setContentType("text/plain");
        actualBootstrap.store(resp.getWriter(), "Galasa Bootstrap Properties");// NOSONAR //TODO catch this as SQ says
    }

    @Activate
    void activate(Map<String, Object> properties) {
        modified(properties);
        logger.info("Galasa Bootstrap API activated");
    }

    @Modified
    void modified(Map<String, Object> properties) {
        synchronized (configurationProperties) {
            configurationProperties.clear();
            configurationProperties.putAll(properties);
        }
    }

    @Deactivate
    void deactivate() {
        synchronized (configurationProperties) {
            this.configurationProperties.clear();
        }
    }

}
