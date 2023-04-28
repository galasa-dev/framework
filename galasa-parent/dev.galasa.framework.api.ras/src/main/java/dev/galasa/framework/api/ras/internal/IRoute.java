/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IRoute {
    String getPath();
    void handleRequest(HttpServletRequest req, HttpServletResponse res, String runId) throws ServletException, IOException;
}