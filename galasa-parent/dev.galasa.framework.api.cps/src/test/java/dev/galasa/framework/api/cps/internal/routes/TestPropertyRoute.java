/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.cps.internal.routes;
import dev.galasa.framework.api.cps.internal.CpsServletTest;
import dev.galasa.framework.api.cps.internal.mocks.MockCpsServlet;
import dev.galasa.framework.api.cps.internal.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.cps.internal.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.cps.internal.mocks.MockServletOutputStream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class TestPropertyRoute extends CpsServletTest{

    
    /*
     * TESTS
     */
    @Test
    public void TestPropertyRouteGetNoFrameworkReturnError() throws Exception{
		// Given...
		setServlet("/cps/namespace1/properties/property1",null ,new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: ",
			"Error occured when trying to access the endpoint"
		);
    }

    @Test
    public void TestPropertyRouteWithExistingNamespaceReturnsOk() throws Exception {
        // Given...
        setServlet("/cps/framework/properties/property1", "framework", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
        Map<String, String> properties = new HashMap<String,String>();
        properties.put("property1", "value1");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
    }

    @Test
    public void TestPropertyRouteWithExistingNamespaceDifferentPropertyReturnsOk() throws Exception {
        // Given...
        setServlet("/cps/framework/properties/property3", "framework", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
        Map<String, String> properties = new HashMap<String,String>();
        properties.put("property3", "value3");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
    }

    @Test
    public void TestPropertyRouteWithExistingNamespaceBadPropertyNameReturnsEmpty() throws Exception {
        // Given...
        setServlet("/cps/framework/properties/inproperty", "framework", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[]");
    }

    @Test
    public void TestPropertyRouteWithExistingNamespaceIncopmpletePropertyNameReturnsEmpty() throws Exception {
        // Given...
        setServlet("/cps/framework/properties/roperty", "framework", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[]");
    }

        @Test
    public void TestPropertyRouteWithExistingNamespacePUTNewPropertyReturns201StatusEmpty() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = "value6";
        setServlet("/cps/framework/properties/"+propertyName, "framework", value);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(201);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).isEqualTo("Successfully created property property6 in framework");
        assertThat(checkNewPropertyInNamespace(propertyName, value)).isTrue();
    
        

       
    }
}
