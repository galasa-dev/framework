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

import com.google.gson.JsonElement;

import dev.galasa.framework.api.common.resources.AcceptContentType;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.utils.GalasaGson;

import static dev.galasa.framework.api.common.resources.AcceptContentType.*;
import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     * Checks if the HTTP Request contains an "Accept" and if the values of that header are part of the default or the supplied list
     * 
     * @param request the HTTP request to be validated
     * @param supportedTypes The values of the Accept header that should be included in the place of the application/json values
     * @throws InternalServletException if the Accept header does not match the expected values
     */
    protected void checkRequestorAcceptContent(HttpServletRequest request, AcceptContentType... supportedTypes) throws InternalServletException {
        boolean isValid = false;
        String accepts = request.getHeader("Accept");
        if (accepts != null) {
            if (supportedTypes == null || supportedTypes.length == 0) {
                supportedTypes = new AcceptContentType[] { APPLICATION_JSON };
            }
            String[] headerList = accepts.split(",");

            for (AcceptContentType type : supportedTypes) {
                for (String header : headerList) {
                    // Split on the comma separator in case it contains multiple MIME types example: Accept: application/json;q=0.9, */*;q=0.8
                    if (type.isInHeader(header.trim().split(";")[0])) {
                        isValid = true;
                        break;
                    }
                }
            }
            if (!isValid) {
                String supportedTypesStr = Arrays.stream(supportedTypes)
                    .map(AcceptContentType::toString)
                    .collect(Collectors.joining(", "));

                ServletError error = new ServletError(GAL5070_UNSUPPORTED_CONTENT_TYPE_REQUESTED, supportedTypesStr);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
        }
    }

    protected String getResponseType(String requestedAcceptTypes, AcceptContentType... supportedTypes) throws InternalServletException {
        // If no "Accept" header value is set, then use the given default type
        String responseContentType = APPLICATION_JSON.toString();
        if (supportedTypes == null || supportedTypes.length == 0) {
            supportedTypes = new AcceptContentType[] { APPLICATION_JSON };
        }

        if (requestedAcceptTypes != null) {

            // Find the matching types that are supported by the server and were requested by the client
            List<ContentType> supportedAcceptTypes = getSupportedTypesFromAcceptHeader(requestedAcceptTypes, supportedTypes);

            if (!supportedAcceptTypes.isEmpty()) {
                String responseType = supportedAcceptTypes.get(0).getType();

                // If something like "application/*" or "*/*" are given, then we'll still use the default type
                if (!responseType.contains("*")) {
                    responseContentType = responseType;
                }
            } else {
                // The "Accept" header only contained types that aren't supported, so throw an error
                String supportedContentTypesStr = Arrays.stream(supportedTypes)
                    .map(AcceptContentType::toString)
                    .collect(Collectors.joining(", "));

                ServletError error = new ServletError(GAL5070_UNSUPPORTED_CONTENT_TYPE_REQUESTED, supportedContentTypesStr);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
        }
        return responseContentType;
    }

    private List<ContentType> getSupportedTypesFromAcceptHeader(String acceptHeaderContent, AcceptContentType... supportedTypes) {
        List<ContentType> supportedAcceptTypes = new ArrayList<>();
        List<ContentType> parsedAcceptTypes = parseAcceptHeader(acceptHeaderContent);

        for (ContentType acceptType : parsedAcceptTypes) {
            String contentType = acceptType.getType();

            for (AcceptContentType supportedType : supportedTypes) {
                if (supportedType.isInHeader(contentType)) {
                    supportedAcceptTypes.add(acceptType);
                }
            }
        }

        // Sort the supported types based on their quality values
        supportedAcceptTypes = supportedAcceptTypes.stream()
            .sorted((acceptType1, acceptType2) -> Double.compare(acceptType2.getQuality(), acceptType1.getQuality()))
            .collect(Collectors.toList());

        return supportedAcceptTypes;
    }

    /**
     * Parses the value of an "Accept" HTTP header into a map of content types and their associated quality value,
     * used to assign the priority of given types. See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept
     * for details on "Accept" headers
     *
     * @param acceptHeader the value of an "Accept" header, potentially a comma-separated list
     * @return a map of content types and quality values
     */
    private List<ContentType> parseAcceptHeader(String acceptHeader) {
        List<ContentType> contentTypes = new ArrayList<>();

        // Multiple content types may have been set, so split them into tokens
        String[] acceptHeaderTokens = acceptHeader.split(",");

        for (String token : acceptHeaderTokens) {

            // Some content types may be in the form "<content-type>;q=<priority>", so split them into their sub-parts
            // For example: "Accept: application/json;q=0.8, application/yaml;q=0.9"
            String[] tokenParts = token.trim().split(";q=");
            String type = tokenParts[0].trim();

            // If no quality value was given, it defaults to 1 (see https://developer.mozilla.org/en-US/docs/Glossary/Quality_values)
            double quality = 1;
            if (tokenParts.length == 2) {
                try {
                    quality = Double.parseDouble(tokenParts[1].trim());
                } catch (NumberFormatException ex) {
                    logger.warn("Could not parse quality value for content type '" + tokenParts[0] + "', setting quality to 0");
                    quality = 0;
                }
            }
            contentTypes.add(new ContentType(type, quality));
        }
        return contentTypes;
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

    /**
     * Checks the json element to make sure it is not a NULL value or an empty object
     * @param element The json element we want to check
     * @throws InternalServletException
     */
    protected void checkJsonElementIsValidJSON(JsonElement element) throws InternalServletException{
        if ( element.isJsonNull()){
            ServletError error = new ServletError(GAL5067_NULL_RESOURCE_IN_BODY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        if ( element.getAsJsonObject().entrySet().isEmpty()){
            ServletError error = new ServletError(GAL5068_EMPTY_JSON_RESOURCE_IN_BODY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
