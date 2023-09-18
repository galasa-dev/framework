/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.cps.internal.routes;
import dev.galasa.framework.api.cps.internal.CpsServletTest;
import dev.galasa.framework.api.cps.internal.mocks.MockCpsServlet;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class TestPropertyRoute extends CpsServletTest{

    
    /*
     * TESTS  -- GET requests
     */
    @Test
    public void TestPropertyRouteGETNoFrameworkReturnsError() throws Exception{
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
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'namespace1'. The Namespace provided is invalid."
		);
    }

    @Test
    public void TestPropertyRouteGetBadNamespaceReturnsError() throws Exception{
		// Given...
		setServlet("/cps/error/properties/property1",null ,new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'error'. The Namespace provided is invalid."
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

    /*
     * TESTS  -- PUT requests
     */

    @Test
    public void TestPropertyRoutePUTNoFrameworkReturnsError() throws Exception{
		// Given...
		setServlet("/cps/namespace1/properties/property1",null ,"value6", "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPut(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'namespace1'. The Namespace provided is invalid."
		);
    }

    @Test
    public void TestPropertyRoutePUTBadNamespaceReturnsError() throws Exception{
		// Given...
		setServlet("/cps/error/properties/property1",null ,"value6", "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPut(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'error'. The Namespace provided is invalid."
		);
    }

    @Test
    public void TestPropertyRouteWithExistingNamespacePUTNewPropertyReturnsSuccess() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = "value6";
        setServlet("/cps/framework/properties/"+propertyName, "framework", value, "PUT");
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

    @Test
    public void TestPropertyRouteWithExistingNamespacePUTExistingPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property5";
        String value = "value6";
        setServlet("/cps/framework/properties/"+propertyName, "framework", value, "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
       checkErrorStructure(
			outStream.toString(),
			5019,
			"E: Error occured when trying to access property 'property5'.",
            " The property name provided already exists in the 'framework' namesapce."
		);        
    }

    @Test
    public void TestPropertyRouteWithErroneousNamespacePUTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = "value6";
        setServlet("/cps/framew0rk/properties/"+propertyName, "framework", value, "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'framew0rk'. The Namespace provided is invalid."
		); 
    }
    
    @Test
    public void TestPropertyRouteWithNamespaceNoValuePUTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = "";
        setServlet("/cps/framew0rk/properties/"+propertyName, "framework", value, "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(411);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5411,
            "E: Error occured when trying to access the endpoint '/cps/framew0rk/properties/property6'.",
            " The request body is empty."
		); 
    }

    @Test
    public void TestPropertyRouteWithNamespaceNullValuePUTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = null;
        setServlet("/cps/framework/properties/"+propertyName, "framework", value, "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(411);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5411,
            "E: Error occured when trying to access the endpoint '/cps/framework/properties/property6'.",
            " The request body is empty."
		); 
    }

    /*
     * TESTS  -- POST requests
     */
    @Test
    public void TestPropertyRouteGetPOSTFrameworkReturnsError() throws Exception{
		// Given...
		setServlet("/cps/namespace1/properties/property1",null ,"value12", "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPost(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'namespace1'. The Namespace provided is invalid."
		);
    }
    
    @Test
    public void TestPropertyRoutePOSTBadNamespaceReturnsError() throws Exception{
		// Given...
		setServlet("/cps/error/properties/property1",null ,"value6", "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPost(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'error'. The Namespace provided is invalid."
		);
    }

    @Test
    public void TestPropertyRouteWithExistingNamespacePOSTExistingPropertyReturnsOK() throws Exception {
        // Given...
        String propertyName = "property5";
        String value = "value6";
        setServlet("/cps/framework/properties/"+propertyName, "framework", value, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(201);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).isEqualTo("Successfully updated property property5 in framework");
        assertThat(checkNewPropertyInNamespace(propertyName, value)).isTrue();       
    }

    @Test
    public void TestPropertyRouteWithExistingNamespacePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = "value6";
        setServlet("/cps/framework/properties/"+propertyName, "framework", value, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
       checkErrorStructure(
			outStream.toString(),
			5018,
			"E: Error occured when trying to access property 'property6'. The property name provided is invalid."
		);        
    }

    @Test
    public void TestPropertyRouteWithErroneousNamespacePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property5";
        String value = "value6";
        setServlet("/cps/framew0rk/properties/"+propertyName, "framework", value, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5017,
			"E: Error occured when trying to access namespace 'framew0rk'. The Namespace provided is invalid."
		); 
    }
    
    @Test
    public void TestPropertyRouteWithNamespaceNoValuePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property5";
        String value = "";
        setServlet("/cps/framew0rk/properties/"+propertyName, "framework", value, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(411);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5411,
            "E: Error occured when trying to access the endpoint '/cps/framew0rk/properties/property5'.",
            " The request body is empty."
		); 
    }

    @Test
    public void TestPropertyRouteWithNamespaceNullValuePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = null;
        setServlet("/cps/framework/properties/"+propertyName, "framework", value, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(411);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5411,
            "E: Error occured when trying to access the endpoint '/cps/framework/properties/property6'.",
            " The request body is empty."
		); 
    }

    /*
     * TESTS  -- DELETE requests
     */

    @Test
    public void TestPropertyRouteDELETENoFrameworkReturnsError() throws Exception{
        // Given...
		setServlet("/cps/namespace1/properties/property1", null, null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
        
		// When...
		servlet.init();
		servlet.doDelete(req,resp);
        
		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        
		checkErrorStructure(
			outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'namespace1'. The Namespace provided is invalid."
        );
    }
        
    
    @Test
    public void TestPropertyRouteDELETEBadNamespaceReturnsError() throws Exception{
        // Given...
		setServlet("/cps/error/properties/property1", null, null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
        
		// When...
		servlet.init();
		servlet.doDelete(req,resp);
        
		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        
		checkErrorStructure(
            outStream.toString(),
			5017,
			"GAL5017E: Error occured when trying to access namespace 'error'. The Namespace provided is invalid."
            );
        }
        
    @Test
    public void TestPropertyRouteDELETEBadPropertyReturnsError() throws Exception{
        // Given...
		setServlet("/cps/framework/properties/badproperty", "framework", null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
        
		// When...
		servlet.init();
		servlet.doDelete(req,resp);
        
		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        
		checkErrorStructure(
			outStream.toString(),
			5018,
			"E: Error occured when trying to access property 'badproperty'. The property name provided is invalid."
        );
    }

    @Test
    public void TestPropertyRouteDELETEPropertyReturnsOk() throws Exception{
        // Given...
        String propertyName = "property1";
        String value = "value1";
		setServlet("/cps/framework/properties/"+propertyName, "framework", null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
        
		// When...
		servlet.init();
		servlet.doDelete(req,resp);
        
		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to RouteInteger status = resp.getStatus();
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).isEqualTo("Successfully deleted property property1 in framework");
        assertThat(checkNewPropertyInNamespace(propertyName, value)).isFalse();
    }
}
