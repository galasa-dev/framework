/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.openapi.servlet.routes;

import static org.assertj.core.api.Assertions.*;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.openapi.servlet.OpenApiServlet;
import dev.galasa.framework.api.openapi.servlet.mocks.MockOpenApiServlet;
import dev.galasa.framework.spi.utils.GalasaGson;

public class OpenApiRouteTest {

    @Test
    public void testGetOpenApiServesYamlSpecificationOk() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setContentType("application/yaml");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(outputStream.toString()).contains(
            "openapi:",
            "version",
            "title: Galasa Ecosystem API",
            "url: '" + apiServerUrl + "'",
            "paths:",
            "/bootstrap:",
            "/auth/tokens:"
        );
    }

    @Test
    public void testGetOpenApiWithJsonContentTypeServesJsonDocumentOk() throws Exception {
        // Given...
        GalasaGson gson = new GalasaGson();
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setContentType("application/json");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);

        // Check a few key parts of the OpenAPI specification were returned and that the JSON string can be serialised into a JSON object
        JsonObject openApiJson = gson.fromJson(outputStream.toString(), JsonObject.class);
        assertThat(openApiJson.has("openapi")).isTrue();
        assertThat(openApiJson.has("info")).isTrue();
        assertThat(openApiJson.has("paths")).isTrue();

        JsonObject openApiInfo = openApiJson.get("info").getAsJsonObject();
        assertThat(openApiInfo.has("version")).isTrue();
        assertThat(openApiInfo.has("title")).isTrue();
        assertThat(openApiInfo.get("title").getAsString()).isEqualTo("Galasa Ecosystem API");

        JsonObject openApiPaths = openApiJson.get("paths").getAsJsonObject();
        assertThat(openApiPaths.has("/bootstrap")).isTrue();
        assertThat(openApiPaths.has("/auth/tokens")).isTrue();
        assertThat(openApiPaths.has("/openapi")).isTrue();
    }

    @Test
    public void testGetOpenApiWithoutContentTypeThrowsError() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Make sure no content type is set
        request.setContentType(null);

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(outputStream.toString()).contains("GAL5071E", "Missing 'Content-Type' header in request");
    }

    @Test
    public void testGetOpenApiWithBlankContentTypeThrowsError() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Set a blank content type
        request.setContentType("");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(outputStream.toString()).contains("GAL5071E", "Missing 'Content-Type' header in request");
    }

    @Test
    public void testGetOpenApiWithInvalidContentTypeThrowsError() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setContentType("this-is-not-a-valid-content-type!");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(outputStream.toString()).contains("GAL5070E", "Invalid 'Content-Type' header value set");
    }
}
