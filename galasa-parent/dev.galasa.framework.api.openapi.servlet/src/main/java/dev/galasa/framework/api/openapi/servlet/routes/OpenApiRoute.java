/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.openapi.servlet.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import dev.galasa.framework.api.common.BaseRoute;
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

    public OpenApiRoute(ResponseBuilder responseBuilder, String apiServerUrl) throws IOException {
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
        String responseContentType = validateContentType(request.getContentType());

        // Convert the YAML contents into a JSON string if the user expects a JSON response
        String responseContents = openApiYamlContents;
        if (responseContentType.equals(APPLICATION_JSON)) {
            responseContents = openApiJsonContents;
        }
        return getResponseBuilder().buildResponse(request, response, responseContentType, responseContents, HttpServletResponse.SC_OK);
    }

    private String getOpenApiFileAsYamlString() throws IOException {
        logger.info("Reading OpenAPI specification");
        String contents = "";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(OPENAPI_FILE_NAME)) {
            contents = new String(stream.readAllBytes());

            // Replace the templated API server URL with the ecosystem's actual API server URL
            if (apiServerUrl != null) {
                contents = contents.replace(OPENAPI_SERVER_TEMPLATE, apiServerUrl);
            }
        }
        logger.info("OpenAPI specification retrieved OK");
        return contents;
    }

    private String validateContentType(String requestedContentType) throws InternalServletException {
        // Throw a 'Bad Request' error if an invalid content type was requested or if no content type was set
        String supportedContentTypesStr = String.join(", ", SUPPORTED_CONTENT_TYPES);
        if (requestedContentType == null || requestedContentType.isEmpty()) {
            ServletError error = new ServletError(GAL5071_NO_CONTENT_TYPE_SET, supportedContentTypesStr);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (!SUPPORTED_CONTENT_TYPES.contains(requestedContentType)) {
            ServletError error = new ServletError(GAL5070_INVALID_CONTENT_TYPE_REQUESTED, supportedContentTypesStr);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return requestedContentType;
    }

    private String convertYamlToJson(String yamlContent) {
        return gson.toJson(new Yaml().load(yamlContent));
    }
}
