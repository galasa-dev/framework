/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.utils.GalasaGson;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.BufferedReader;
import java.io.IOException;

public abstract class BaseRoute implements IRoute {

    protected static final GalasaGson gson = new GalasaGson();

    protected Log logger = LogFactory.getLog(this.getClass());

	private final ResponseBuilder responseBuilder ;

    private final String path;

    public BaseRoute(ResponseBuilder responseBuilder, String path) {
        this.path = path;
		this.responseBuilder = responseBuilder;
    }

    public String getPath() {
        return path;
    }

	public ResponseBuilder getResponseBuilder() {
		return this.responseBuilder;
	}

    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, 
            HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, FrameworkException {
        throwMethodNotAllowedException(request, pathInfo);
        return response;
    }

    public HttpServletResponse handlePutRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, FrameworkException {
        throwMethodNotAllowedException(request, pathInfo);
        return response;
    }

    public HttpServletResponse handlePostRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        throwMethodNotAllowedException(request, pathInfo);
        return response;
    }

    public HttpServletResponse handleDeleteRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, FrameworkException {
        throwMethodNotAllowedException(request, pathInfo);
        return response;
    }

    private void throwMethodNotAllowedException(HttpServletRequest request, String pathInfo) throws InternalServletException {
        ServletError error = new ServletError(GAL5405_METHOD_NOT_ALLOWED, pathInfo, request.getMethod());
        throw new InternalServletException(error, HttpServletResponse.SC_METHOD_NOT_ALLOWED);        
    }

    protected boolean checkRequestHasContent(HttpServletRequest request) throws InternalServletException {
        boolean valid = false;
        try {
            if (request.getContentLength() > 0){
                valid = true;
            }
        } catch (NullPointerException e ){
            // Catch the NullPointerException (empty request body) to throw error in if 
        }  
        if (!valid){
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY, request.getPathInfo());
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
        return valid;
    }

    /**
     * Parses a given HTTP request's body into a bean object
     * 
     * @param request the HTTP request to be parsed
     * @param clazz the bean class to parse the request body into
     * @return the parsed HTTP request body
     * @throws IOException if there is an issue reading the request body
     * @throws InternalServletException if the request does not contain a body
     */
    protected <T> T parseRequestBody(HttpServletRequest request, Class<T> clazz) throws IOException, InternalServletException {
        StringBuilder sbRequestBody = new StringBuilder();
        checkRequestHasContent(request);

        try (BufferedReader bodyReader = request.getReader()) {
            String line = bodyReader.readLine();
            while (line != null) {
                sbRequestBody.append(line);
                line = bodyReader.readLine();
            }
        }
        return gson.fromJson(sbRequestBody.toString(), clazz);
    }
}
