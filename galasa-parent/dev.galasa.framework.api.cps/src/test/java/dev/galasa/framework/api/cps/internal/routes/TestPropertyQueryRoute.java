
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


public class TestPropertyQueryRoute extends CpsServletTest{
    @Test
    public void TestPropertyQueryNoFrameworkReturnError() throws Exception{
		// Given...
		setServlet("/cps/namespace1/properties",null ,new HashMap<String,String[]>());
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
        setServlet("/cps/framework/properties", "framework", new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
        Map<String, String> properties = new HashMap<String,String>();
        properties.put("property1", "value1");
        properties.put("property2", "value2");
        properties.put("property3", "value3");
        properties.put("property4", "value4");
        properties.put("property5", "value5");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
        
    }

    @Test
    public void TestPropertyQueryHiddenNamespaceReturnsError() throws Exception {
        // Given...
		setServlet("/cps/dss/properties", "dss" ,new HashMap<String,String[]>());
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
			"Error occured when trying to access namespace 'dss'. Namespace 'dss' is not available"
		);
    }

	@Test
    public void TestPropertyQueryInvalidNamespaceReturnsError() throws Exception {
        // Given...
		setServlet("/cps/j!ndex/properties", "framework" ,new HashMap<String,String[]>());
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
			5017,
			"GAL5017E: ",
			"Error occured when trying to access namespace 'j!ndex'. The Namespace provided is invalid"
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
		Map<String, String> results = new PropertyQueryRoute(null,null).getPropertiesWithSuffix(properties, suffix);
		
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
		Map<String, String> results = new PropertyQueryRoute(null,null).getPropertiesWithSuffix(properties, suffix);
		
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
		Map<String, String> results = new PropertyQueryRoute(null,null).getPropertiesWithSuffix(properties, suffix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithPrefixNoMatchReturnsEmpty() {
		//Given...
		String prefix  = "crate";
		Map<String, String> expectedProperties = new HashMap<String,String>();

		Map<String, String> properties = new HashMap<String,String>();
		properties.put("property2", "value2");
		properties.put("property3", "value3");
		properties.put("property4", "value4");
		properties.put("property5", "value5");
		properties.put("property6", "value6");

		//When...
		Map<String, String> results = new PropertyQueryRoute(null,null).getPropertiesWithPrefix(properties, prefix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithPrefixReturnsOneRecord() {
		//Given...
		String prefix  = "pre";
		Map<String, String> expectedProperties = new HashMap<String,String>();
		expectedProperties.put("preperty1", "value1");
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("property2", "value2");
		properties.put("property3", "value3");
		properties.put("property4", "value4");
		properties.put("property5", "value5");
		properties.put("property6", "value6");
		properties.putAll(expectedProperties);

		//When...
		Map<String, String> results = new PropertyQueryRoute(null,null).getPropertiesWithPrefix(properties, prefix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithPrefixReturnsFiveRecord() {
		//Given...
		String prefix  = ".";
		Map<String, String> expectedProperties = new HashMap<String,String>();
		expectedProperties.put(".property", "value1");
		expectedProperties.put(".charity", "value2");
		expectedProperties.put(".hospitality", "value3");
		expectedProperties.put(".aunty", "value4");
		expectedProperties.put(".empty", "value5");
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("property2", "value6");
		properties.put("property3", "value7");
		properties.put("property4", "value8");
		properties.put("property5", "value9");
		properties.put("property6", "value10");
		properties.putAll(expectedProperties);

		//When...
		Map<String, String> results = new PropertyQueryRoute(null,null).getPropertiesWithPrefix(properties, prefix);
		
		//Then...
		assertThat(results).isEqualTo(expectedProperties);
	}

    @Test
    public void TestPropertyQueryWithNamespaceAndURLQuerySuffixReturnsOneRecord() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("suffix", new String[] {"1"});

        setServlet("/cps/framework/properties?suffix=1", "framework", params);
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
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"property1\",\n    \"value\": \"value1\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithSuffixThatDoesNotExistReturnsEmpty() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("suffix", new String[] {"mickey"});

        setServlet("/cps/framework/properties?suffix=mickey", "framework", params);
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
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithSuffixReturnsMultipleRecords() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("suffix", new String[] {"ty"});

        setServlet("/cps/multi/properties?suffix=mickey", "multi", params);
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
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \".hospitality\",\n    \"value\": \"value3\"\n  },"+
			"\n  {\n    \"name\": \"test.property\",\n    \"value\": \"value1\"\n  },\n  {\n    \"name\": \"test.empty\",\n    \"value\": \"value5\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryPrefixReturnsOneRecord() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {".char"});

        setServlet("/cps/multi/properties?prefix=.char", "multi", params);
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
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \".charity1\",\n    \"value\": \"value2\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixThatDoesNotExistReturnsEmpty() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"goof"});

        setServlet("/cps/framework/properties?prefix=goof", "framework", params);
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
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixReturnsMultipleRecords() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"test"});

        setServlet("/cps/multi/properties?prefix=test", "multi", params);
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
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"test.property\",\n    \"value\": \"value1\"\n  },"+
			"\n  {\n    \"name\": \"test.empty\",\n    \"value\": \"value5\"\n  },\n  {\n    \"name\": \"test.aunty5\",\n    \"value\": \"value4\"\n  }\n]" );
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryPrefixAndSuffixReturnsOneRecord() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"prop"});
		params.put("suffix", new String[] {"5"});

        setServlet("/cps/framework/properties?prefix=prop&suffix=5", "framework", params);
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
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \"property5\",\n    \"value\": \"value5\"\n  }\n]");
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixAndSuffixThatDoesNotExistReturnsEmpty() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"hello"});
		params.put("suffix", new String[] {"world"});

        setServlet("/cps/framework/properties?prefix=hellosuffix=world", "framework", params);
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
        assertThat(resp.getStatus()==200);
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

        setServlet("/cps/multi/properties?prefix=.&suffix=1", "multi", params);
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
        assertThat(resp.getStatus()==200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		assertThat(output).isEqualTo("[\n  {\n    \"name\": \".charity1\",\n    \"value\": \"value2\"\n  },"+
			"\n  {\n    \"name\": \".lecture101\",\n    \"value\": \"value101\"\n  }\n]");
    }


}