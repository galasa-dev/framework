/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.GalasaPropertyName;
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

	private Map<GalasaPropertyName, CPSProperty> getPropertiesFromMap (Map<String,String> inputMap){
		Map<GalasaPropertyName, CPSProperty> properties = new HashMap<GalasaPropertyName, CPSProperty>();
		for (Map.Entry<String,String> prop: inputMap.entrySet()){
			properties.put(new GalasaPropertyName(prop.getKey()), new CPSProperty(prop.getKey(), prop.getValue()));
		}
		return properties;
	}

	private void checkPropertiesMatch(Map<GalasaPropertyName,CPSProperty> results, Map<GalasaPropertyName,CPSProperty> expected){
		assertThat(results.size()).isEqualTo(expected.size());
		if (expected.size() > 0){
			for (Map.Entry<GalasaPropertyName,CPSProperty> prop : expected.entrySet()){
				GalasaPropertyName expectedKey = prop.getKey();
				assertThat(results.get(expectedKey)).usingRecursiveComparison().isEqualTo(expected.get(expectedKey));
			}
		}
	}

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
        properties.put(namespace+".property.1", "value1");
        properties.put(namespace+".property.2", "value2");
        properties.put(namespace+".property.3", "value3");
        properties.put(namespace+".property.4", "value4");
        properties.put(namespace+".property.5", "value5");

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
        properties.put(namespace+".property.1", "********");
        properties.put(namespace+".property.2", "********");
        properties.put(namespace+".property.3", "********");
        properties.put(namespace+".property.4", "********");
        properties.put(namespace+".property.5", "********");

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
		Map<GalasaPropertyName, CPSProperty> expectedProperties = new HashMap<GalasaPropertyName, CPSProperty>();
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("property.2", "value2");
		properties.put("property.3", "value3");
		properties.put("property.4", "value4");
		properties.put("property.5", "value5");
		properties.put("property.6", "value6");
		Map<GalasaPropertyName, CPSProperty> props = getPropertiesFromMap(properties);

		//When...
		Map<GalasaPropertyName, CPSProperty> results = new PropertyRoute(null,null).filterPropertiesBySuffix(props, suffix);
		
		//Then...
		checkPropertiesMatch(results, expectedProperties);
	}

	@Test
	public void TestGetPropertiesWithSuffixReturnsOneRecord() {
		//Given...
		String suffix  = "1";
		Map<String, String> expectedProperties = new HashMap<String,String>();
		expectedProperties.put("framework.property.1", "value1");
		Map<GalasaPropertyName, CPSProperty> expectedProps = getPropertiesFromMap(expectedProperties);
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("framework.property.2", "value2");
		properties.put("framework.property.3", "value3");
		properties.put("framework.property.4", "value4");
		properties.put("framework.property.5", "value5");
		properties.put("framework.property.6", "value6");
		properties.putAll(expectedProperties);
		Map<GalasaPropertyName, CPSProperty> props = getPropertiesFromMap(properties);

		//When...
		Map<GalasaPropertyName,CPSProperty> results = new PropertyRoute(null,null).filterPropertiesBySuffix(props, suffix);
		
		//Then...
		checkPropertiesMatch(results, expectedProps);
	}

	@Test
	public void TestGetPropertiesWithSuffixReturnsFiveRecord() {
		//Given...
		String suffix  = "ty";
		Map<String, String> expectedProperties = new HashMap<String,String>();
		expectedProperties.put("proper.ty", "value1");
		expectedProperties.put("chari.ty", "value2");
		expectedProperties.put("hospitali.ty", "value3");
		expectedProperties.put("aun.ty", "value4");
		expectedProperties.put("emp.ty", "value5");
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("property.2", "value6");
		properties.put("property.3", "value7");
		properties.put("property.4", "value8");
		properties.put("property.5", "value9");
		properties.put("property.6", "value10");
		properties.putAll(expectedProperties);
		Map<GalasaPropertyName, CPSProperty> expectedProps = getPropertiesFromMap(expectedProperties);
		Map<GalasaPropertyName, CPSProperty> props = getPropertiesFromMap(properties);

		//When...
		Map<GalasaPropertyName, CPSProperty> results = new PropertyRoute(null,null).filterPropertiesBySuffix(props, suffix);
		
		//Then...
		checkPropertiesMatch(results, expectedProps);
	}

	@Test
	public void TestGetPropertiesWithPrefixNoMatchReturnsEmpty() {
		//Given...
		String namespace = "framework";
		String prefix  = "crate";
		Map<GalasaPropertyName, CPSProperty> expectedProperties = new HashMap<GalasaPropertyName, CPSProperty>();

		Map<String, String> properties = new HashMap<String,String>();
		properties.put(namespace+".property1","value1");
		properties.put(namespace+".property2", "value2");
		properties.put(namespace+".property3", "value3");
		properties.put(namespace+".property4", "value4");
		properties.put(namespace+".property5", "value5");
		properties.put(namespace+".property6", "value6");
		Map<GalasaPropertyName, CPSProperty> props = getPropertiesFromMap(properties);

		//When...
		Map<GalasaPropertyName, CPSProperty> results = new PropertyRoute(null,null).filterPropertiesByPrefix(namespace, props, prefix);
		
		//Then...
		checkPropertiesMatch(results, expectedProperties);
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
		Map<GalasaPropertyName, CPSProperty> expectedProps = getPropertiesFromMap(expectedProperties);
		Map<GalasaPropertyName, CPSProperty> props = getPropertiesFromMap(properties);

		//When...
		Map<GalasaPropertyName, CPSProperty> results = new PropertyRoute(null,null).filterPropertiesByPrefix(namespace, props, prefix);
		
		//Then...
		checkPropertiesMatch(results, expectedProps);
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
		Map<GalasaPropertyName, CPSProperty> expectedProps = getPropertiesFromMap(expectedProperties);
		Map<GalasaPropertyName, CPSProperty> props = getPropertiesFromMap(properties);

		//When...
		Map<GalasaPropertyName, CPSProperty> results = new PropertyRoute(null,null).filterPropertiesByPrefix(namespace, props, prefix);
		
		//Then...
		checkPropertiesMatch(results, expectedProps);
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
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("framework.property.1", "value1");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
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

        setServlet("/multi/properties?suffix=ty", "multi", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("multi.example.hospitality", "value3");
		properties.put("multi.test.empty", "value5");
		properties.put("multi.test.property", "value1");
        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryPrefixReturnsOneRecord() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"example.char"});

        setServlet("/multi/properties?prefix=example.char", "multi", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("multi.example.charity1", "value2");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
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
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("multi.test.aunty5", "value4");
		properties.put("multi.test.empty", "value5");
		properties.put("multi.test.property", "value1");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
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
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("framework.property.5", "value5");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
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
		params.put("prefix", new String[] {"example"});
		params.put("suffix", new String[] {"1"});

        setServlet("/multi/properties?prefix=example&suffix=1", "multi", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("multi.example.charity1", "value2");
		properties.put("multi.example.lecture101", "value101");

		// Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
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
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("infixes.test.property.testing.local.stream", "value3");
		properties.put("infixes.test.property.testing.stream", "value4");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
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
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("infixes.test.property.testing.local.stream", "value3");
		properties.put("infixes.test.property.testing.stream", "value4");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixSuffixAndInfixesReturnsMultipleRecords() throws Exception {
        // Given...
		Map <String,String[]> params = new HashMap<String,String[]>();
		params.put("prefix", new String[] {"test"});
		params.put("suffix", new String[] {"stream"});
		params.put("infix", new String[] {"property.,testing,local"});

        setServlet("/infixes/properties?prefix=test&suffix=stream&infix=property,testing,local", "infixes", params);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doGet(req, resp);
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("infixes.test.property.testing.local.stream", "value3");
		properties.put("infixes.test.property.testing.stream", "value4");
		properties.put("infixes.test.aproperty.stream", "value1");
		properties.put("infixes.test.bproperty.stream", "value2");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
    }

	@Test
    public void TestPropertyQueryWithNamespaceAndURLQueryWithPrefixSuffixAndInfixWithTwoSegmentsReturnsOk() throws Exception {
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
		Map<String, String> properties = new HashMap<String,String>();
		properties.put("infixes.test.property.testing.local.stream", "value3");
		properties.put("infixes.test.property.testing.stream", "value4");

        // Then...
        // We expect data back
        String output = outStream.toString();
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
		checkJsonArrayStructure(output,properties);
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
		String namespace = "framework";
        String propertyName = "property.6";
        String value = "value6";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/namespace1/properties",null ,propertyJSON, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPost(req,resp);

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
    public void TestPropertyRoutePOSTBadNamespaceReturnsError() throws Exception{
		// Given...
		String namespace = "error";
        String propertyName = "property.6";
        String value = "value6";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/error/properties",null ,propertyJSON, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPost(req,resp);

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
    public void TestPropertyRouteWithExistingNamespacePOSTNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyName = "property.6";
        String value = "value6";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/framework/properties", namespace, propertyJSON , "POST");
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
        assertThat(output).isEqualTo("Successfully created property property.6 in framework");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();       
    }

	@Test
    public void TestPropertyRouteNamespaceWithMiddleCapitalLetterPOSTNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "camelCase";
        String propertyName = "property.";
        String value = "value";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/camelCase/properties", namespace, propertyJSON , "POST");
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
			5404,
			"Error occured when trying to identify the endpoint '/camelCase/properties'. "+
			"Please check your endpoint URL or report the problem to your Galasa Ecosystem owner."
		);
    }

	@Test
    public void TestPropertyRouteNamespaceBeginningWithCapitalLetterPOSTNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "NotCamelcase";
        String propertyName = "property.";
        String value = "value";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/NotCamelcase/properties", namespace, propertyJSON , "POST");
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
			5404,
			"Error occured when trying to identify the endpoint '/NotCamelcase/properties'. "+
			"Please check your endpoint URL or report the problem to your Galasa Ecosystem owner."
		);     
    }

	@Test
    public void TestPropertyRouteNamespaceEndingWithCapitalLetterPOSTNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "notcamelcasE";
        String propertyName = "property.";
        String value = "value";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/notcamelcasE/properties", namespace, propertyJSON , "POST");
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
			5404,
			"Error occured when trying to identify the endpoint '/notcamelcasE/properties'. "+
			"Please check your endpoint URL or report the problem to your Galasa Ecosystem owner."
		);   
    }

	@Test
    public void TestPropertyRouteNamespaceWithNumberAtMiddlePOSTNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "camel3Case";
        String propertyName = "property.";
        String value = "value";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/camel3Case/properties", namespace, propertyJSON , "POST");
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
			5404,
			"Error occured when trying to identify the endpoint '/camel3Case/properties'. "+
			"Please check your endpoint URL or report the problem to your Galasa Ecosystem owner."
		);      
    }

	@Test
    public void TestPropertyRouteNamespaceEndingWithNumberAtStartPOSTNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "camelCase3";
        String propertyName = "property.";
        String value = "value";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/camelCase3/properties", namespace, propertyJSON , "POST");
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
			5404,
			"Error occured when trying to identify the endpoint '/camelCase3/properties'. "+
			"Please check your endpoint URL or report the problem to your Galasa Ecosystem owner."
		);         
    }

	@Test
    public void TestPropertyRouteNamespaceWithMultipleNumbersPOSTNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "c4ame6lcas5e8";
        String propertyName = "property.newproperty";
        String value = "value";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		JsonElement requestJson = JsonParser.parseString(propertyJSON);
		String requestBody = requestJson.toString();
		setServlet("/c4ame6lcas5e8/properties", namespace, requestBody , "POST");
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
        assertThat(output).isEqualTo("Successfully created property property.newproperty in c4ame6lcas5e8");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();       
    }

	@Test
    public void TestPropertyRouteNamespaceWithNewNamespacePOSTNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "newnamespace";
        String propertyName = "property.newproperty";
        String value = "value";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		JsonElement requestJson = JsonParser.parseString(propertyJSON);
		String requestBody = requestJson.toString();
		setServlet("/newnamespace/properties", "framework", requestBody , "POST");
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
        assertThat(output).isEqualTo("Successfully created property property.newproperty in newnamespace");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();       
    }

	@Test
    public void TestPropertyRouteWithProtectedNamespacePOSTNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "secure";
        String propertyName = "property.6";
        String value = "value6";
		String propertyJSON = generatePropertyJSON(namespace, propertyName, value, "galasa-dev/v1alpha1");
		JsonElement requestJson = JsonParser.parseString(propertyJSON);
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
        assertThat(output).isEqualTo("Successfully created property property.6 in secure");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();       
    }

	@Test
    public void TestPropertyRouteWithHiddenNamespacePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property.6";
        String value = "value6";
		String propertyJSON = generatePropertyJSON("dss", propertyName, value, "galasa-dev/v1alpha1");
		setServlet("/dss/properties", "framework", propertyJSON , "POST");
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
        String propertyName = "property.5";
        String value = "value6";
		String propertyJSON = generatePropertyJSON("framework", propertyName, value, "galasa-dev/v1alpha1");
        setServlet("/framework/properties", "framework", propertyJSON, "POST");
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
			"E: Error occured when trying to access property 'property.5'.",
            " The property name provided already exists in the 'framework' namespace."
		);        
    }

    @Test
    public void TestPropertyRouteWithErroneousNamespacePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property.6";
        String value = "value6";
		String propertyJSON = generatePropertyJSON("notframew0rk", propertyName, value, "galasa-dev/v1alpha1");
        setServlet("/framew0rk/properties", "framework", propertyJSON, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(400);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5028,
			"GAL5028E: The GalasaProperty namespace 'notframew0rk' must match the url namespace 'framew0rk'."
		); 
    }

	@Test
    public void TestPropertyRouteWithDifferentNamespacePOSTNewPropertyReturnsError() throws Exception {
        // Given...
        String propertyName = "property.6";
        String value = "value6";
		String propertyJSON = generatePropertyJSON("secure", propertyName, value, "galasa-dev/v1alpha1");
        setServlet("/framework/properties", "framework", propertyJSON, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(400);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");

		checkErrorStructure(
			outStream.toString(),
			5028,
			"GAL5028E: The GalasaProperty namespace 'secure' must match the url namespace 'framework'."
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