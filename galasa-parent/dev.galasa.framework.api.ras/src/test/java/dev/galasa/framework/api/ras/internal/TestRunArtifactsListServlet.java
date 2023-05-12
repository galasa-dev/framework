/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IRunResult;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.*;


import dev.galasa.framework.api.ras.internal.mocks.*;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockPath;

import static org.assertj.core.api.Assertions.*;

import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.nio.file.Path;

public class TestRunArtifactsListServlet extends BaseServletTest {	

	class MockJsonObject {
		public Path path;
		public String contentType;
		public int size;

		public MockJsonObject(Path path, String contentType, int size) {
			this.path = path;
			this.contentType = contentType;
			this.size = size;
		}
	}

	public String generateExpectedJson(List<MockJsonObject> artifacts, int size) {
		String jsonResult = "[\n";
		int numOfArtifacts = artifacts.size();
		if (numOfArtifacts > 0) {
			
			for (int i = 0; i < numOfArtifacts; i++ ) {
				String runData = "";
				if (0 < i && i < numOfArtifacts) {
					runData = ",\n";
				}
				runData += "  {\n"+
					   "    \"path\": \""+artifacts.get(i).path.toString()+"\",\n"+
					   "    \"contentType\": \""+artifacts.get(i).contentType+"\",\n"+
					   "    \"size\": "+artifacts.get(i).size+"\n"+
					   "  }";
				jsonResult += runData;
			}
			String structureData= ",\n  {\n"+
					   "    \"path\": \"/structure.json\",\n"+
					   "    \"contentType\": \"application/json\",\n"+
					   "    \"size\": 71\n"+
					   "  }";
			jsonResult += structureData;
			String artifactsData= ",\n  {\n"+
					   "    \"path\": \"/artifacts.properties\",\n"+
					   "    \"contentType\": \"text/plain\",\n"+
					   "    \"size\": "+size+"\n"+
					   "  }";
			jsonResult += artifactsData;
		}
		
		jsonResult += "\n]";
		return jsonResult;
	}

	@Before
	public void setUp() {
		mockFileSystem = new MockFileSystem();
	}

    @Test
	public void testMultipleArtifactsToListReturnsOKWithArtifacts() throws Exception {
		//Given..
		String runName = "testA";
		MockPath mockArtifactsPath = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		List<Path> dummyArtifactPaths = Arrays.asList(
			new MockPath(mockArtifactsPath + "/dummyB.gz",mockFileSystem),
			new MockPath(mockArtifactsPath + "/dummyC.txt",mockFileSystem),
			new MockPath(mockArtifactsPath + "/dummyA.json",mockFileSystem)
			);
		
		List<MockJsonObject> mockArtifacts = Arrays.asList(
			new MockJsonObject(dummyArtifactPaths.get(0), "application/x-gzip", 0),
			new MockJsonObject(dummyArtifactPaths.get(1), "text/plain", 0),
			new MockJsonObject(dummyArtifactPaths.get(2), "application/json", 0)
		);

		mockFileSystem.createDirectories(mockArtifactsPath);
		for (Path artifactPath : dummyArtifactPaths) {
			mockFileSystem.createFile(artifactPath);

		}
		
		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		parameterMap.put("runId", new String[] {runId} );

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// [
		//	 {
		//     "path": "/testA/artifacts/dummyA.gz",
		//     "contentType": "application/x-gzip",
		//     "size": "0",
		//   },
		//	 {
		//     "path": "/testA/artifacts/dummyB.gz",
		//     "contentType": "application/x-gzip",
		//     "size": "0",
		//   },
		//	 {
		//     "path": "/testA/artifacts/dummyC.gz",
		//     "contentType": "application/x-gzip",
		//     "size": "0",
		//   },
		//	 {
		//     "path": "/run.log",
		//     "contentType": "text/plain",
		//     "size": "0",
		//   },
		// ]
		assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(5);

		String expectedJson = generateExpectedJson(mockArtifacts, 240);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
	
		assertThat(resp.getContentType()).isEqualTo("Application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testOneArtifactToListReturnsOKWithArtifact() throws Exception {
		//Given..
		String runName = "testA";
		MockPath mockArtifactsPath = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		MockPath dummyArtifactPath = new MockPath(mockArtifactsPath + "/dummy.gz",mockFileSystem);
		MockJsonObject mockArtifact = new MockJsonObject(dummyArtifactPath, "application/x-gzip", 0);
		mockFileSystem.createDirectories(mockArtifactsPath);
		mockFileSystem.createFile(dummyArtifactPath);

		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// [
		//	 {
		//     "path": "/testA/artifacts/dummy.gz",
		//     "contentType": "application/x-gzip",
		//	   "size": 0
		//   },
		//	 {
		//     "path": "/structure.json",
		//     "contentType": "application/json",
		//     "size": "82",
		//   },
		//	 {
		//     "path": "/artifacts.properties",
		//     "contentType": "text/plain",
		//     "size": "240",
		//   }
		// ]
		// assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(3);

		String expectedJson = generateExpectedJson(Arrays.asList(mockArtifact), 82);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
	
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}


    @Test
	public void testNoArtifactsToListGivesRootArtifacts() throws Exception {
		//Given..
		String runName = "testA";
		MockFileSystem mockFileSystem = new MockFileSystem();
		MockPath mockArtifactsPath = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		mockFileSystem.createDirectories(mockArtifactsPath);

		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// [
		//	 {
		//     "path": "/structure.json",
		//     "contentType": "application/json",
		//     "size": "82",
		//   },
		//	 {
		//     "path": "/artifacts.properties",
		//     "contentType": "text/plain",
		//     "size": "240",
		//   }
		// ]
		assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(2);
	
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}

	@Test
	public void testBadRunIdGivesNotFoundError() throws Exception {
		//Given..
		String runId = "badRunId";
		List<IRunResult> mockInputRunResults = new ArrayList<>();

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts");
		MockBaseServletEnvironment mockServletEnvironment = new MockBaseServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);
		
		BaseServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();
		
		//When...
		servlet.activate();
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'badRunId'.""
		// }
		assertThat(resp.getStatus()).isEqualTo(404);
		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "badRunId" );
	
		assertThat( resp.getContentType()).isEqualTo("Application/json");
		assertThat( resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
	}
}