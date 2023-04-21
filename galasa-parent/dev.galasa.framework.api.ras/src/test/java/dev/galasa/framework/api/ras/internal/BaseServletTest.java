/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import com.google.gson.*;

import static org.assertj.core.api.Assertions.*;


public class BaseServletTest {

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
}