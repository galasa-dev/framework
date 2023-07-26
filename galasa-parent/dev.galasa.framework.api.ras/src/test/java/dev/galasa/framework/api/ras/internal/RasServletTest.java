/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import com.google.gson.*;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.api.ras.internal.mocks.MockRunResult;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockPath;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class RasServletTest {

	protected MockFileSystem mockFileSystem;

	protected void checkErrorStructure(String jsonString , int expectedErrorCode , String... expectedErrorMessageParts ) throws Exception {

		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonObject jsonObject = jsonElement.getAsJsonObject();
		assertThat(jsonObject).isNotNull().as("Json parsed is not a json object.");

		// Check the error code
		JsonElement errorCodeField = jsonObject.get("error_code");
		assertThat(errorCodeField).isNotNull().as("Returned structure didn't contain the error_code field!");

		int actualErrorCode = jsonObject.get("error_code").getAsInt();
		assertThat(actualErrorCode).isEqualTo(expectedErrorCode);

		// Check the error message...
		String msg = jsonObject.get("error_message").toString();
		for ( String expectedMessagePart : expectedErrorMessageParts ) {
			assertThat(msg).contains(expectedMessagePart);
		}
	}

	protected void checkJsonArrayStructure(String jsonString, Map<String, String> jsonFieldsToCheck) throws Exception {

		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray).isNotNull().as("Json parsed is not a json array.");

        // Go through the map of provided fields and check if any of the objects in the JSON array
        // contain a matching key-value entry.
        for (Entry<String, String> entry : jsonFieldsToCheck.entrySet()) {
            boolean fieldMatches = false;

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.get(entry.getKey()).toString().equals(entry.getValue())) {
                    fieldMatches = true;
                }
            }
            assertThat(fieldMatches).isTrue();
        }
    }

	protected List<IRunResult> generateTestData(String runId, String runName, String runLog) {
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();

		// Build the results the DB will return.
		String requestor = "galasa";
		
		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setResult("Passed");

		Path artifactRoot = new MockPath("/" + runName, this.mockFileSystem);
		IRunResult result = new MockRunResult( runId, testStructure, artifactRoot, runLog);
		mockInputRunResults.add(result);

		return mockInputRunResults;
	}
}