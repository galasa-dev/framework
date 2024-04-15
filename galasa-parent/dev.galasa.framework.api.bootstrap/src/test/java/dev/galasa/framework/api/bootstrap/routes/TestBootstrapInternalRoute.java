/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import dev.galasa.framework.api.bootstrap.BootstrapServletTest;
import dev.galasa.framework.api.bootstrap.mocks.MockBootstrapServlet;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.spi.FrameworkException;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
public class TestBootstrapInternalRoute extends BootstrapServletTest {

    @Test
    public void TestBootstrapInternalRouteHandleGetRequestReturnsOK() throws ServletException, IOException, FrameworkException{
        // Given...
        Properties configurationProperties = new Properties();
        configurationProperties.setProperty("framework.config.store", "mystore");
        configurationProperties.setProperty("framework.extra.bundles", "more.bundles");
        configurationProperties.setProperty("framework.testcatalog.url", "myeco.dev/testcatalog");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        BootstrapInternalRoute route = new BootstrapInternalRoute(responseBuilder, configurationProperties);
        HttpServletResponse response = (HttpServletResponse) new MockHttpServletResponse();
        ServletOutputStream outStream = response.getOutputStream();

        // When...
        response = route.handleGetRequest("/bootstrap",null,null,response);

        // Then...
        String output = outStream.toString();
        assertThat(output).contains("#Galasa Bootstrap Properties\n",
            "framework.config.store=mystore\n",
            "framework.extra.bundles=more.bundles\n",
            "framework.testcatalog.url=myeco.dev/testcatalog\n");
    }

    @Test
    public void TestBootstrapInternalRequestReturnsProperties() throws Exception {
        // Given...
        Map<String, Object> properties = Map.of(
            "framework.config.store", "mystore",
            "framework.testcatalog.url", "myeco.dev/testcatalog"
        );

        setServlet("");
        MockBootstrapServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.activate(properties);
        servlet.doGet(req, resp);


        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).contains("#Galasa Bootstrap Properties\n",
            "framework.config.store=mystore\n",
            "framework.testcatalog.url=myeco.dev/testcatalog\n");
    }

    @Test
    public void TestBootstrapInternalRequestWithNoPropertiesReturnsHeaderOnly() throws Exception {
        // Given...
        Map<String, Object> properties = new HashMap<>();

        setServlet("");
        MockBootstrapServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.activate(properties);
        servlet.doGet(req, resp);

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).contains("#Galasa Bootstrap Properties");
        assertThat(output).doesNotContain("framework.", "=");
    }
}