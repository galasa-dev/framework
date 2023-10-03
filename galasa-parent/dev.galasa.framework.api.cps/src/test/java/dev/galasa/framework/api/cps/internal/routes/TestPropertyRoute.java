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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class TestPropertyRoute extends CpsServletTest{
    @Test
    public void TestPropertyQueryNoFrameworkReturnsError() throws Exception{
		// Given...
		setServlet("/namespace1/properties",null ,new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server couldn't find any Etcd store to query
		assertThat(resp.getStatus()==500);
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
    public void TestPropertyQueryWithExistingNamespaceReturnsOk() throws Exception {
        // Given...
		String namespace = "framework";
        setServlet("/framework/properties", namespace, new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
        Map<String, String> properties = new HashMap<String,String>();
        properties.put(namespace+".property1", "value1");
        properties.put(namespace+".property2", "value2");
        properties.put(namespace+".property3", "value3");
        properties.put(namespace+".property4", "value4");
        properties.put(namespace+".property5", "value5");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
        
    }

	@Test
    public void TestPropertyQueryWithProtectedNamespaceReturnsOk() throws Exception {
        // Given...
		String namespace = "secure";
        setServlet("/secure/properties", namespace, new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
        Map<String, String> properties = new HashMap<String,String>();
        properties.put(namespace+".property1", "********");
        properties.put(namespace+".property2", "********");
        properties.put(namespace+".property3", "********");
        properties.put(namespace+".property4", "********");
        properties.put(namespace+".property5", "********");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
        
    }

    @Test
    public void TestPropertyQueryHiddenNamespaceReturnsError() throws Exception {
        // Given...
		setServlet("/dss/properties", "dss" ,new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server could find the namespace, but it was hidden
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
    public void TestPropertyQueryInvalidNamespaceReturnsError() throws Exception {
        // Given...
		setServlet("/j!ndex/properties", "framework" ,new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server could find the namespace
		assertThat(resp.getStatus()==500);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5404,
			"GAL5404E:",
			" Error occured when trying to identify the endpoint '/j!ndex/properties'. ",
			"Please check your endpoint URL or report the problem to your Galasa Ecosystem owner."
		);
	}

	@Test
	public void TestGetPropertiesWithSuffixNoMatchReturnsEmpty() {
		//Given...
		String suffix  = "rty1";
		Map<String, String> expectedProperties = new HashMap<String,String>();

		Map<String, String> properties = new HashMap<String,String>();
		properties.put("property2", "value2");
		properties.put("property3", "value3");
		properties.put("property4", "value4");
		properties.put("property5", "value5");
		properties.put("property6", "value6");

		//When...
		Map<String, String> results = new PropertyRoute(null,null).filterPropertiesBySuffix(properties, suffix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithSuffixReturnsOneRecord() {
		//Given...
		String suffix  = "1";
		Map<String, String> expectedProperties = new HashMap<String,String>();
		expectedProperties.put("property1", "value1");
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("property2", "value2");
		properties.put("property3", "value3");
		properties.put("property4", "value4");
		properties.put("property5", "value5");
		properties.put("property6", "value6");
		properties.putAll(expectedProperties);

		//When...
		Map<String, String> results = new PropertyRoute(null,null).filterPropertiesBySuffix(properties, suffix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithSuffixReturnsFiveRecord() {
		//Given...
		String suffix  = "ty";
		Map<String, String> expectedProperties = new HashMap<String,String>();
		expectedProperties.put("property", "value1");
		expectedProperties.put("charity", "value2");
		expectedProperties.put("hospitality", "value3");
		expectedProperties.put("aunty", "value4");
		expectedProperties.put("empty", "value5");
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("property2", "value6");
		properties.put("property3", "value7");
		properties.put("property4", "value8");
		properties.put("property5", "value9");
		properties.put("property6", "value10");
		properties.putAll(expectedProperties);

		//When...
		Map<String, String> results = new PropertyRoute(null,null).filterPropertiesBySuffix(properties, suffix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithPrefixNoMatchReturnsEmpty() {
		//Given...
		String namespace = "framework";
		String prefix  = "crate";
		Map<String, String> expectedProperties = new HashMap<String,String>();

		Map<String, String> properties = new HashMap<String,String>();
		properties.put(namespace+".property1","value1");
		properties.put(namespace+".property2", "value2");
		properties.put(namespace+".property3", "value3");
		properties.put(namespace+".property4", "value4");
		properties.put(namespace+".property5", "value5");
		properties.put(namespace+".property6", "value6");

		//When...
		Map<String, String> results = new PropertyRoute(null,null).filterPropertiesByPrefix(namespace, properties, prefix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithPrefixReturnsOneRecord() {
		//Given...
		String namespace = "framework";
		String prefix  = "pre";
		Map<String, String> expectedProperties = new HashMap<String,String>();
		expectedProperties.put(namespace+".preperty1", "value1");
		Map<String, String> properties = new HashMap<String,String>();
		properties.put(namespace+".property2", "value2");
		properties.put(namespace+".property3", "value3");
		properties.put(namespace+".property4", "value4");
		properties.put(namespace+".property5", "value5");
		properties.put(namespace+".property6", "value6");
		properties.putAll(expectedProperties);

		//When...
		Map<String, String> results = new PropertyRoute(null,null).filterPropertiesByPrefix(namespace, properties, prefix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithPrefixReturnsFiveRecord() {
		//Given...
		String namespace ="framework";
		String prefix  = ".";
		Map<String, String> expectedProperties = new HashMap<String,String>();
		expectedProperties.put(namespace+"..property", "value1");
		expectedProperties.put(namespace+"..charity", "value2");
		expectedProperties.put(namespace+"..hospitality", "value3");
		expectedProperties.put(namespace+"..aunty", "value4");
		expectedProperties.put(namespace+"..empty", "value5");
		Map<String, String> properties = new HashMap<String,String>();
		properties.put(namespace+".property2", "value6");
		properties.put(namespace+".property3", "value7");
		properties.put(namespace+".property4", "value8");
		properties.put(namespace+".property5", "value9");
		properties.put(namespace+".property6", "value10");
		properties.putAll(expectedProperties);

		//When...
		Map<String, String> results = new PropertyRoute(null,null).filterPropertiesByPrefix(namespace, properties, prefix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

    @Test
    public void TestPropertyQueryWithNamespaceAndURLQuerySuffixReturnsOneRecord() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("suffix", new String[] {"1"});

        setServlet("/framework/properties?suffix=1", "framework", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"framework.property1\",\n    \"value\": \"value1\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithSuffixThatDoesNotExistReturnsEmpty() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("suffix", new String[] {"mickey"});

        setServlet("/framework/properties?suffix=mickey", "framework", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithSuffixReturnsMultipleRecords() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("suffix", new String[] {"ty"});

        setServlet("/multi/properties?suffix=mickey", "multi", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"multi.test.property\",\n    \"value\": \"value1\"\n  },"+
			"\n  {\n    \"name\": \"multi..hospitality\",\n    \"value\": \"value3\"\n  },\n  {\n    \"name\": \"multi.test.empty\",\n    \"value\": \"value5\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryPrefixReturnsOneRecord() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {".char"});

        setServlet("/multi/properties?prefix=.char", "multi", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"multi..charity1\",\n    \"value\": \"value2\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixThatDoesNotExistReturnsEmpty() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"goof"});

        setServlet("/framework/properties?prefix=goof", "framework", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixReturnsMultipleRecords() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"test"});

        setServlet("/multi/properties?prefix=test", "multi", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"multi.test.property\",\n    \"value\": \"value1\"\n  },"+
			"\n  {\n    \"name\": \"multi.test.aunty5\",\n    \"value\": \"value4\"\n  },\n  {\n    \"name\": \"multi.test.empty\",\n    \"value\": \"value5\"\n  }\n]" );
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryPrefixAndSuffixReturnsOneRecord() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"prop"});
		params.put("suffix", new String[] {"5"});

        setServlet("/framework/properties?prefix=prop&suffix=5", "framework", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"framework.property5\",\n    \"value\": \"value5\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixAndSuffixThatDoesNotExistReturnsEmpty() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"hello"});
		params.put("suffix", new String[] {"world"});

        setServlet("/framework/properties?prefix=hellosuffix=world", "framework", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixAndSuffixReturnsMultipleRecords() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"."});
		params.put("suffix", new String[] {"1"});

        setServlet("/multi/properties?prefix=.&suffix=1", "multi", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"multi..lecture101\",\n    \"value\": \"value101\"\n  },"+
			"\n  {\n    \"name\": \"multi..charity1\",\n    \"value\": \"value2\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixSuffixAndInfixReturnsMultipleRecords() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"test"});
		params.put("suffix", new String[] {"stream"});
		params.put("infix", new String[] {"testing"});

        setServlet("/infixes/properties?prefix=test&suffix=stream&infix=testing", "infixes", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"infixes.test.property.testing.stream\",\n    \"value\": \"value4\"\n  },"+
			"\n  {\n    \"name\": \"infixes.test.property.testing.local.stream\",\n    \"value\": \"value3\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixSuffixAndInfixesReturnsMultipleRecordsOK() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"test"});
		params.put("suffix", new String[] {"stream"});
		params.put("infix", new String[] {"testing, local"});

        setServlet("/infixes/properties?prefix=test&suffix=stream&infix=testing,local", "infixes", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"infixes.test.property.testing.stream\",\n    \"value\": \"value4\"\n  },"+
			"\n  {\n    \"name\": \"infixes.test.property.testing.local.stream\",\n    \"value\": \"value3\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixSuffixAndInfixesReturnsMultipleRecords() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"test"});
		params.put("suffix", new String[] {"stream"});
		params.put("infix", new String[] {"property,testing,local"});

        setServlet("/infixes/properties?prefix=test&suffix=stream&infix=property,testing,local", "infixes", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"infixes.test.property.testing.stream\",\n    \"value\": \"value4\"\n  },"+
			"\n  {\n    \"name\": \"infixes.test.aproperty.stream\",\n    \"value\": \"value1\"\n  },"+
			"\n  {\n    \"name\": \"infixes.test.property.testing.local.stream\",\n    \"value\": \"value3\"\n  },"+
			"\n  {\n    \"name\": \"infixes.test.bproperty.stream\",\n    \"value\": \"value2\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixSuffixAndInfixWithTwoSegmentsReturnsOk() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"test"});
		params.put("suffix", new String[] {"stream"});
		params.put("infix", new String[] {"property.testing,local"});

        setServlet("/infixes/properties?prefix=test&suffix=stream&infix=property,testing,local", "infixes", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"infixes.test.property.testing.stream\",\n    \"value\": \"value4\"\n  },"+
			"\n  {\n    \"name\": \"infixes.test.property.testing.local.stream\",\n    \"value\": \"value3\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixSuffixAndBadInfixReurnsEmpty() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"test"});
		params.put("suffix", new String[] {"stream"});
		params.put("infix", new String[] {"properties.testing"});

        setServlet("/infixes/properties?prefix=test&suffix=stream&infix=properties.testing", "infixes", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[]");
    }

	/*
	 * TEST - HANDLE PUT REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixAndSuffixPUTRequestsReturnsError() throws Exception{
		// Given...
		setServlet("/multi/properties?prefix=.&suffix=1","multi",null, "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPut(req,resp);

		// Then...
		// We expect an error back, because the API server has thrown a ConfigurationPropertyStoreException
		assertThat(resp.getStatus()).isEqualTo(405);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5405,
			"E: Error occured when trying to access the endpoint '/multi/properties?prefix=.&suffix=1'. The method 'PUT' is not allowed."
		);
    }

	/*
	 * TEST - HANDLE DELETE REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixAndSuffixDELETERequestsReturnsError() throws Exception{
		// Given...
		setServlet("/multi/properties?prefix=.&suffix=1","multi",null, "DELETE");
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
			"E: Error occured when trying to access the endpoint '/multi/properties?prefix=.&suffix=1'. The method 'DELETE' is not allowed."
		);
    }

	/*
	 * TEST - HANDLE POST REQUEST
	 */
	@Test
    public void TestPropertyRoutePOSTNoFrameworkReturnsError() throws Exception{
		// Given...
		setServlet("/namespace1/properties",null ,"{name: property ,value: value }", "POST");
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
			5016,
			"GAL5016E: Error occured when trying to access namespace 'namespace1'. The namespace provided is invalid."
		);
    }

    @Test
    public void TestPropertyRoutePOSTBadNamespaceReturnsError() throws Exception{
		// Given...
		setServlet("/error/properties",null ,"{name: property ,value: value }", "POST");
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
			5016,
			"GAL5016E: Error occured when trying to access namespace 'error'. The namespace provided is invalid."
		);
    }

    @Test
    public void TestPropertyRouteWithExistingNamespacePOSTNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyName = "property6";
        String value = "value6";
		JsonElement requestJson = JsonParser.parseString(String.format("{name: %s ,value: %s }",propertyName,value));
		String requestBody = requestJson.toString();
		setServlet("/framework/properties", namespace, requestBody , "POST");
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
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).isEqualTo("Successfully created property property6 in framework");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();       
    }

	@Test
    public void TestPropertyRouteWithProtectedNamespacePOSTNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "secure";
        String propertyName = "property6";
        String value = "value6";
		JsonElement requestJson = JsonParser.parseString(String.format("{name: %s ,value: %s }",propertyName,value));
		String requestBody = requestJson.toString();
		setServlet("/secure/properties", namespace, requestBody , "POST");
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
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).isEqualTo("Successfully created property property6 in secure");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();       
    }

	@Test
    public void TestPropertyRouteWithHiddenNamespacePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = "value6";
		JsonElement requestJson = JsonParser.parseString(String.format("{name: %s ,value: %s }",propertyName,value));
		String requestBody = requestJson.toString();
		setServlet("/dss/properties", "framework", requestBody , "POST");
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
			5016,
			"GAL5016E: Error occured when trying to access namespace 'dss'. The namespace provided is invalid."
		);    
    }

    @Test
    public void TestPropertyRouteWithExistingNamespacePOSTExistingPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property5";
        String value = "value6";
		JsonElement requestJson = JsonParser.parseString(String.format("{name: %s ,value: %s }",propertyName,value));
		String requestBody = requestJson.toString();
        setServlet("/framework/properties", "framework", requestBody, "POST");
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
			"E: Error occured when trying to access property 'property5'.",
            " The property name provided already exists in the 'framework' namespace."
		);        
    }

    @Test
    public void TestPropertyRouteWithErroneousNamespacePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property6";
        String value = "value6";
		JsonElement requestJson = JsonParser.parseString(String.format("{name: %s ,value: %s }",propertyName,value));
		String requestBody = requestJson.toString();
        setServlet("/framew0rk/properties", "framework", requestBody, "POST");
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
			5016,
			"GAL5016E: Error occured when trying to access namespace 'framew0rk'. The namespace provided is invalid."
		); 
    }
    
    @Test
    public void TestPropertyRouteWithNamespaceNoRequestBodyPOSTNewPropertyReturnsError() throws Exception {
        // Given...
		String requestBody = "";
        setServlet("/framew0rk/properties", "framework", requestBody, "POST");
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
            "E: Error occured when trying to access the endpoint '/framew0rk/properties'.",
            " The request body is empty."
		); 
    }

    @Test
    public void TestPropertyRouteWithNamespaceNullRequestBodyPOSTNewPropertyReturnsError() throws Exception {
        // Given...
		String requestBody = null;
        setServlet("/framework/properties", "framework", requestBody, "POST");
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
            "E: Error occured when trying to access the endpoint '/framework/properties'.",
            " The request body is empty."
		); 
    }
}