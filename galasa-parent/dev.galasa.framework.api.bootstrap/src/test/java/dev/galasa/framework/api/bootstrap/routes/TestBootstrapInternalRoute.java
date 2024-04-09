/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import dev.galasa.framework.api.bootstrap.BootstrapServletTest;
import dev.galasa.framework.api.bootstrap.mocks.MockBootstrapServlet;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
public class TestBootstrapInternalRoute extends BootstrapServletTest {

    @Test
    public void TestBootstrapInternalRouteHandleGetRequestReturnsOK () throws ServletException, IOException, FrameworkException{
        // Given...
        IConfigurationPropertyStoreService store =  new MockIConfigurationPropertyStoreService("bootstrap");
        store.setProperty("framework.config.store","mystore");
        store.setProperty("framework.extra.bundles","more.bundles");
        store.setProperty("framework.testcatalog.url", "myeco.dev/testcatalog");
        IFramework framework = new MockFramework(store);
        ResponseBuilder responseBuilder = new ResponseBuilder();
        BootstrapInternalRoute route = new BootstrapInternalRoute(responseBuilder, framework);
        HttpServletResponse  response =(HttpServletResponse) new MockHttpServletResponse();
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
    public void TestBootstrapInternalRouteHandleGetRequestWithNullValuesReturnsOK () throws Exception{
        // Given...
        IConfigurationPropertyStoreService store =  new MockIConfigurationPropertyStoreService("bootstrap");
        store.setProperty("framework.config.store","mystore");
        store.setProperty("framework.extra.bundles",null);
        store.setProperty("framework.testcatalog.url", "myeco.dev/testcatalog");
        IFramework framework = new MockFramework(store);
        ResponseBuilder responseBuilder = new ResponseBuilder();
        BootstrapInternalRoute route = new BootstrapInternalRoute(responseBuilder, framework);
        HttpServletResponse  response =(HttpServletResponse) new MockHttpServletResponse();
        ServletOutputStream outStream = response.getOutputStream();
        // When...
        response = route.handleGetRequest("/bootstrap",null,null,response);
        // Then...
        String output = outStream.toString();
        assertThat(output).contains("#Galasa Bootstrap Properties\n",
            "framework.config.store=mystore\n",
            "framework.testcatalog.url=myeco.dev/testcatalog\n");
        assertThat(output).doesNotContain("framework.extra.bundles=more.bundles");
    }

    @Test
    public void TestBootstrapInternalRequestReturnsProperties() throws Exception{
        // Given...
        setServlet("");
        MockBootstrapServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);
 

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).contains("#Galasa Bootstrap Properties\n",
            "framework.config.store=mystore\n",
            "framework.extra.bundles=more.bundles\n",
            "framework.testcatalog.url=myeco.dev/testcatalog\n");
    }
}