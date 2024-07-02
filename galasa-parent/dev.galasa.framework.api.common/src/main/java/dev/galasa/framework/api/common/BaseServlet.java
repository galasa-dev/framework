/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.utils.GalasaGson;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected Log logger = LogFactory.getLog(this.getClass());

    static final GalasaGson gson = new GalasaGson();

    private final Map<String, IRoute> routes = new HashMap<>();

    private ResponseBuilder responseBuilder = new ResponseBuilder();

    protected void addRoute(IRoute route) {
        String path = route.getPath();
        logger.info("Base servlet adding route " + path);
        routes.put(path, route);
    }

    protected ResponseBuilder getResponseBuilder() {
        return this.responseBuilder;
    }

    public void setResponseBuilder(ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.info("BaseServlet: doGet() entered. Url: " + req.getPathInfo());
        processRequest(req, res);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.info("BaseServlet: doPost() entered");
        processRequest(req, res);
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.info("BaseServlet: doPut() entered");
        processRequest(req, res);
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.info("BaseServlet: doDelete() entered");
        processRequest(req, res);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res) {
        String errorString = "";
        int httpStatusCode = HttpServletResponse.SC_OK;

        try {
            processRoutes(req, res);
        } catch (InternalServletException ex) {
            // the message is a curated servlet message, we intentionally threw up to this level.
            errorString = ex.getMessage();
            httpStatusCode = ex.getHttpFailureCode();
            logger.error(errorString, ex);
            getResponseBuilder().buildResponse(req, res, "application/json", errorString, httpStatusCode);
        } catch (Throwable t) {
            // We didn't expect this failure to arrive. So deliver a generic error message.
            errorString = new ServletError(GAL5000_GENERIC_API_ERROR).toJsonString();
            httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            logger.error(errorString, t);
            getResponseBuilder().buildResponse(req, res, "application/json", errorString, httpStatusCode);
        }
    }

    private void processRoutes(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException, FrameworkException, InterruptedException {
        String url = req.getPathInfo();
        if (url == null) {
            // There is no path information, so this must be a root path (e.g. /cps)
            url = "";
        }

        QueryParameters queryParameters = new QueryParameters(req.getParameterMap());

        boolean pathMatched = false;
        for (Map.Entry<String, IRoute> entry : routes.entrySet()) {

            String routePattern = entry.getKey();
            IRoute route = entry.getValue();

            Matcher matcher = Pattern.compile(routePattern).matcher(url);

            if (matcher.matches()) {
                pathMatched = true;
                logger.info("BaseServlet: Found a route that matches.");
                if (req.getMethod().contains("PUT")){
                    route.handlePutRequest(url, queryParameters, req, res);
                } else if (req.getMethod().contains("POST")){
                    route.handlePostRequest(url, queryParameters, req, res);
                } else if (req.getMethod().contains("DELETE")){
                    route.handleDeleteRequest(url, queryParameters, req, res);
                } else {
                    route.handleGetRequest(url, queryParameters, req, res);
                }
                break;
            }
        }

        if (!pathMatched) {
            // No matching route was found, throw a 404 error.
            logger.info("BaseServlet: No matching route found.");
            ServletError error = new ServletError(GAL5404_UNRESOLVED_ENDPOINT_ERROR, url);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
    }
}