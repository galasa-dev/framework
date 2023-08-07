/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.teststructure.TestStructure;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.framework.api.ras.internal.mocks.*;
import static org.assertj.core.api.Assertions.*;

import java.util.*;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestTestClassesRoute extends RasServletTest{

    final static Gson gson = GalasaGsonBuilder.build();

    public List<IRunResult> generateTestData (int resSize){
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		// Build the results the DB will return.
		for(int c =0 ; c < resSize; c++){
			String runId = RandomStringUtils.randomAlphanumeric(16);
			TestStructure testStructure = new TestStructure();
			switch (c % 5){
                case 0: 
					testStructure.setBundle("Bundle1");
					testStructure.setTestName("testName1");
					break;
				case 1:  
					testStructure.setBundle("Bundle2");
					testStructure.setTestName("testName2");
					break;
				case 2: 
					testStructure.setBundle("Bundle3");
					testStructure.setTestName("testName3");
					break;
                case 3: 
					testStructure.setBundle("Bundle4");
					testStructure.setTestName("testName4");
					break;
                case 4: 
					testStructure.setBundle("Bundle5");
					testStructure.setTestName("testName5");
					break;
			}
			IRunResult result = new MockRunResult( runId, testStructure, null , null);
			mockInputRunResults.add(result);
		}
		return mockInputRunResults;
	}

    private String generateExpectedJSON (List<IRunResult> mockInputRunResults, boolean reverse) throws ResultArchiveStoreException{

        HashMap<String,RasTestClass> tests = new HashMap<>();
        String key;
        for (IRunResult run : mockInputRunResults){
			TestStructure testStructure = run.getTestStructure();
			key = testStructure.getBundle()+"/"+testStructure.getTestName();
			if(!tests.containsKey(key)){
				tests.put(key,new RasTestClass(testStructure.getTestName(), testStructure.getBundle()));
			}
        }
        List<RasTestClass> testClasses = new ArrayList<>(tests.values());
        
        testClasses.sort(Comparator.comparing(RasTestClass::getTestClass));
        if (reverse == true) {
            testClasses.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
        }
		JsonElement jsonResultsArray = new Gson().toJsonTree(testClasses);
		JsonObject json = new JsonObject();
		json.add("testclasses", jsonResultsArray);
		return json.toString();
    }

    /*
     * Tests 
     */

	@Test
	public void testRequestorsWithOneTestNoURLQueryReturnsSingleResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(1);
		//Build Http query parameters
 
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);
 
		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
 
		//When...
		servlet.init();
		servlet.doGet(req,resp);
 
		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, false);
		System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testRequestorsWithThreeTestsNoURLQueryReturnsSingleResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(3);
		//Build Http query parameters
 
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);
 
		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
 
		//When...
		servlet.init();
		servlet.doGet(req,resp);
 
		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, false);
		System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testRequestorsWithTenTestsNoURLQueryReturnsSingleResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, false);
		System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testRequestorsWithTenTestsAndSortAscendingURLQueryReturnsSingleResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters
 
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		parameterMap.put("sort", new String[] {"testclass:asc"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);
 
		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
 
		//When...
		servlet.init();
		servlet.doGet(req,resp);
 
		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, false);
		System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testRequestorsWithTenTestsAndSortDescendingURLQueryReturnsSingleResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters
 
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		parameterMap.put("sort", new String[] {"testclass:desc"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);
 
		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
 
		//When...
		servlet.init();
		servlet.doGet(req,resp);
 
		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, true);
		System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

}