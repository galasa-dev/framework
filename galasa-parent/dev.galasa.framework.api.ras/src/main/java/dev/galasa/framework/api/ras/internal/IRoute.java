/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;

import javax.servlet.ServletException;

import dev.galasa.framework.spi.FrameworkException;

public interface IRoute {
    String getPath();

    String handleRequest(String pathInfo, QueryParameters queryParams) throws ServletException, IOException, FrameworkException;
}