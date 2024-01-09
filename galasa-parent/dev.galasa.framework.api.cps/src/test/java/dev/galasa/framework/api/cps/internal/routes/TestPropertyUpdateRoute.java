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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class TestPropertyUpdateRoute extends CpsServletTest{

    
    /*
     * TESTS  -- GET requests
     */
    @Test
    public void TestPropertyRouteGETNoFrameworkReturnsError() throws Exception{
		// Given...
		setServlet("/namespace1/properties/property1",null ,new HashMap<String,String[]>());
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
			5016,
			"GAL5016E: Error occured when trying to access namespace 'namespace1'. The namespace provided is invalid."
		);
    }

    @Test
    public void TestPropertyRouteGETBadNamespaceReturnsError() throws Exception{
		// Given...
		setServlet("/error/properties/property1",null ,new HashMap<String,String[]>());
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
			5016,
			"E: Error occured when trying to access namespace 'error'. The namespace provided is invalid."
		);
    }

    @Test
    public void TestPropertyRouteGETWithExistingNamespaceReturnsOk() throws Exception {
        // Given...
        setServlet("/framework/properties/property.1", "framework", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
		String expectedJson = generateExpectedJson("framework", "property.1", "value1", "galasa-dev/v1alpha1");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo(expectedJson);
    }

	@Test
    public void TestPropertyRouteGETWithProtectedNamespaceReturnsOk() throws Exception {
        // Given...
        setServlet("/secure/properties/property.1", "secure", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
		String expectedJson = generateExpectedJson("secure", "property.1", "********", "galasa-dev/v1alpha1");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo(expectedJson);
    }

	@Test
    public void TestPropertyRouteGETWithHiddenNamespaceReturnsError() throws Exception {
        // Given...
        setServlet("/dss/properties/property.1", "framework", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        assertThat(resp.getStatus()==500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5016,
			"GAL5016E: ",
			"Error occured when trying to access namespace 'dss'. The namespace provided is invalid"
		);
    }

    @Test
    public void TestPropertyRouteGETWithExistingNamespaceDifferentPropertyReturnsOk() throws Exception {
        // Given...
        setServlet("/framework/properties/property.3", "framework", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
		String expectedJson = generateExpectedJson("framework", "property.3", "value3", "galasa-dev/v1alpha1");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo(expectedJson);
    }

    @Test
    public void TestPropertyRouteGETWithExistingNamespaceBadPropertyNameReturnsEmpty() throws Exception {
        // Given...
        setServlet("/framework/properties/in.property", "framework", new HashMap<String,String[]>());
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
    public void TestPropertyRouteGETWithExistingNamespaceIncopmpletePropertyNameReturnsEmpty() throws Exception {
        // Given...
        setServlet("/framework/properties/roperty.1", "framework", new HashMap<String,String[]>());
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
    public void TestPropertyRoutePUTFrameworkReturnsError() throws Exception{
		// Given...
		String json = generatePropertyJSON("namespace1", "property.1", "value12", "galasa-dev/v1alpha1");
		setServlet("/namespace1/properties/property.1","error" ,json, "PUT");
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
			5016,
			"GAL5016E: Error occured when trying to access namespace 'namespace1'. The namespace provided is invalid."
		);
    }
    
    @Test
    public void TestPropertyRoutePUTBadNamespaceReturnsError() throws Exception{
		// Given...
		String json = generatePropertyJSON("error", "property.1", "value12", "galasa-dev/v1alpha1");
		setServlet("/error/properties/property1","error" ,json, "PUT");
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
			5029,
			"The GalasaProperty name 'property.1' must match the url namespace 'property1'."
		);
    }

    @Test
    public void TestPropertyRouteWithExistingNamespacePUTExistingPropertyReturnsOK() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyName = "property.5";
        String value = "value6";
		String json = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
        setServlet("/framework/properties/"+propertyName, namespace, json, "PUT");
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
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).isEqualTo("Successfully updated property property.5 in framework");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();       
    }

	@Test
    public void TestPropertyRouteWithProtectedNamespacePUTExistingPropertyReturnsOK() throws Exception {
        // Given...
		String namespace = "secure";
        String propertyName = "property.5";
        String value = "value6";
		String json = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
        setServlet("/secure/properties/"+propertyName, namespace, json, "PUT");
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
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).isEqualTo("Successfully updated property property.5 in secure");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();
    }

	@Test
    public void TestPropertyRoutePUTWithHiddenNamespaceReturnsError() throws Exception {
        // Given...
		String namespace = "dss";
        String propertyName = "property.5";
        String value = "value6";
		String json = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
        setServlet("/dss/properties/"+propertyName, "framework", json, "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        // We expect data back
        assertThat(resp.getStatus()==500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5016,
			"GAL5016E: ",
			"Error occured when trying to access namespace 'dss'. The namespace provided is invalid"
		);
    }

    @Test
    public void TestPropertyRouteWithExistingNamespacePUTNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyName = "property.6";
        String value = "value6";
		String json = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
        setServlet("/framework/properties/"+propertyName, namespace, json, "PUT");
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
			5017,
			"E: Error occured when trying to access property 'property.6'. The property name provided is invalid."
		);        
    }

    @Test
    public void TestPropertyRouteWithErroneousNamespacePUTNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyName = "property.5";
        String value = "value6";
		String json = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
        setServlet("/framew0rk/properties/"+propertyName, namespace, json, "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(400);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5028,
			"GAL5028E: The GalasaProperty namespace 'framework' must match the url namespace 'framew0rk'."
		); 
    }
    
    @Test
    public void TestPropertyRouteWithNamespaceNoValuePUTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property5";
        String value = "";
        setServlet("/framew0rk/properties/"+propertyName, "framework", value, "PUT");
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
            "E: Error occured when trying to access the endpoint '/framew0rk/properties/property5'.",
            " The request body is empty."
		); 
    }

    @Test
    public void TestPropertyRouteWithNamespaceNullValuePUTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = null;
        setServlet("/framework/properties/"+propertyName, "framework", value, "PUT");
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
            "E: Error occured when trying to access the endpoint '/framework/properties/property6'.",
            " The request body is empty."
		); 
    }

    /*
     * TESTS  -- DELETE requests
     */

    @Test
    public void TestPropertyRouteDELETENoFrameworkReturnsError() throws Exception{
        // Given...
		setServlet("/namespace1/properties/property1", null, null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
        
		// When...
		servlet.init();
		servlet.doDelete(req,resp);
        
		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        
		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: Error occured when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner."
        );
    }
        
	@Test
    public void TestPropertyRouteDELETEWithHiddenNamespaceReturnsError() throws Exception {
        // Given...
        String propertyName = "property.5";
        setServlet("/dss/properties/"+propertyName, "framework", null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        assertThat(resp.getStatus()==500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5016,
			"GAL5016E: ",
			"Error occured when trying to access namespace 'dss'. The namespace provided is invalid"
		);
    }
    
    @Test
    public void TestPropertyRouteDELETEBadNamespaceReturnsError() throws Exception{
        // Given...
		setServlet("/error/properties/property1", null, null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
        
		// When...
		servlet.init();
		servlet.doDelete(req,resp);
        
		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		assertThat(resp.getStatus()).isEqualTo(500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        
		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: Error occured when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner."
        );
        }
        
    @Test
    public void TestPropertyRouteDELETEBadPropertyReturnsErrorOK() throws Exception{
        // Given...
		setServlet("/framework/properties/bad.property", "framework", null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
        
		// When...
		servlet.init();
		servlet.doDelete(req,resp);
        
		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to Route
		String output = outStream.toString();
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("Successfully deleted property bad.property in framework");
        
    }

    @Test
    public void TestPropertyRouteDELETEPropertyReturnsOk() throws Exception{
        // Given...
		String namespace = "framework";
        String propertyName = "property.1";
        String value = "value1";
		setServlet("/framework/properties/"+propertyName, namespace, null, "DELETE");
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
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).isEqualTo("Successfully deleted property property.1 in framework");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isFalse(); 
    }

	@Test
    public void TestPropertyRouteDELETEPropertyinProtectedNamespaceReturnOK() throws Exception{
        // Given...
		String namespace = "secure";
        String propertyName = "property.1";
		String value = "value1";
		setServlet("/secure/properties/"+propertyName, namespace, value, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
        
		// When...
		servlet.init();
		servlet.doDelete(req,resp);
        
		// Then...
		String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("Successfully deleted property property.1 in secure");
		assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isFalse(); 
    }

	/*
	 * TEST - HANDLE POST REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void TestPropertyRoutePOSTPropertyReturnsError() throws Exception{
		// Given...
		String namespace = "framework";
        String propertyName = "property1";
        String value = "value1";
		setServlet("/framework/properties/"+propertyName, namespace, value, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doDelete(req,resp);

		// Then...
		// We expect an error back, because the API server has thrown a ConfigurationPropertyStoreException
		assertThat(resp.getStatus()).isEqualTo(405);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5405,
			"E: Error occured when trying to access the endpoint '/framework/properties/property1'. The method 'POST' is not allowed."
		);
    }
}