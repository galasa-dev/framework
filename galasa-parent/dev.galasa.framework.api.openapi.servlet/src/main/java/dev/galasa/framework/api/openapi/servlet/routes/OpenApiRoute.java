/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.openapi.servlet.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.ContentType;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.utils.GalasaGson;

/**
 * This is the base /openapi route that is used to serve the Galasa API server's OpenAPI specification,
 * either in YAML or JSON format, depending on the MIME type requested.
 */
public class OpenApiRoute extends BaseRoute {

    private static final String OPENAPI_SERVER_TEMPLATE = "{API_SERVER_URL}";
    private static final String OPENAPI_FILE_NAME = "openapi.yaml";

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_YAML = "application/yaml";
    private static final List<String> SUPPORTED_CONTENT_TYPES = List.of(APPLICATION_YAML, APPLICATION_JSON);

    private static final GalasaGson gson = new GalasaGson();

    private final String openApiYamlContents;
    private final String openApiJsonContents;

    private Log logger = LogFactory.getLog(this.getClass());
    private String apiServerUrl;

    public OpenApiRoute(ResponseBuilder responseBuilder, String apiServerUrl) throws IOException, InternalServletException {
        super(responseBuilder, "");
        this.apiServerUrl = apiServerUrl;

        // The OpenAPI file is small so it can be loaded into memory and served when required
        // instead of reading it on every request to this endpoint
        openApiYamlContents = getOpenApiFileAsYamlString();
        openApiJsonContents = convertYamlToJson(openApiYamlContents);
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("OpenApiRoute: handleGetRequest() Entered");
        String requestAcceptedTypes = request.getHeader("Accept");
        String responseContentType = getResponseType(requestAcceptedTypes);

        // Convert the YAML contents into a JSON string if the user expects a JSON response
        String responseContents = openApiYamlContents;
        if (responseContentType.equals(APPLICATION_JSON)) {
            responseContents = openApiJsonContents;
        }

        logger.info("OpenApiRoute: handleGetRequest() Exiting");
        return getResponseBuilder().buildResponse(request, response, responseContentType, responseContents, HttpServletResponse.SC_OK);
    }

    private String getOpenApiFileAsYamlString() throws IOException {
        logger.info("Reading OpenAPI specification");
        String contents = "";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(OPENAPI_FILE_NAME)) {
            // Replace the templated API server URL with the ecosystem's actual API server URL
            contents = new String(stream.readAllBytes()).replace(OPENAPI_SERVER_TEMPLATE, apiServerUrl);
        }
        logger.info("OpenAPI specification retrieved OK");
        return contents;
    }

    private String getResponseType(String requestedAcceptTypes) throws InternalServletException {
        // If no "Accept" header value is set, then default to JSON
        String responseContentType = APPLICATION_JSON;
        if (requestedAcceptTypes != null) {

            // Find the matching types that are supported by the server and were requested by the client
            List<ContentType> supportedAcceptTypes = getSupportedTypesFromAcceptHeader(requestedAcceptTypes);

            if (!supportedAcceptTypes.isEmpty()) {
                String responseType = supportedAcceptTypes.get(0).getType();

                // If "application/*" or "*/*" are given, then we'll default to "application/json"
                if (!responseType.contains("*")) {
                    responseContentType = responseType;
                }
            } else {
                // The "Accept" header only contained types that aren't supported, so throw an error
                String supportedContentTypesStr = String.join(", ", SUPPORTED_CONTENT_TYPES);
                ServletError error = new ServletError(GAL5070_UNSUPPORTED_CONTENT_TYPE_REQUESTED, supportedContentTypesStr);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
        }
        return responseContentType;
    }

    private List<ContentType> getSupportedTypesFromAcceptHeader(String acceptHeaderContent) {
        List<ContentType> supportedAcceptTypes = new ArrayList<>();
        List<ContentType> parsedAcceptTypes = parseAcceptHeader(acceptHeaderContent);

        for (ContentType acceptType : parsedAcceptTypes) {
            String contentType = acceptType.getType();
            if (SUPPORTED_CONTENT_TYPES.contains(contentType)
                || contentType.equals("application/*")
                || contentType.equals("*/*")
            ) {
                supportedAcceptTypes.add(acceptType);
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

    private String convertYamlToJson(String yamlContent) throws InternalServletException {
        try {
            return gson.toJson(new Yaml().load(yamlContent));
        } catch (Exception ex) {
            ServletError error = new ServletError(GAL5071_FAILED_TO_PARSE_YAML_INTO_JSON);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
        }
    }
}
