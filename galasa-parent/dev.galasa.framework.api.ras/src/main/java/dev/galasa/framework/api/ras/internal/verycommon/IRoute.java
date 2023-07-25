/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.verycommon;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.spi.FrameworkException;

/**
 * IRoute provides methods for endpoints to implement when a request is sent through a servlet,
 * allowing for new routes to be added without needing servlets to know which route handles the request.
 * 
 * Route paths represent the regex patterns that are used to match request paths against.
 */
public interface IRoute {
    String getPath();

    HttpServletResponse handleRequest(String pathInfo, QueryParameters queryParams, HttpServletResponse response) 
        throws ServletException, IOException, FrameworkException;
}
