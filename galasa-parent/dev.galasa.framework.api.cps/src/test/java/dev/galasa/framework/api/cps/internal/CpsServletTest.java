/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal;

import com.google.gson.*;

import dev.galasa.framework.api.cps.internal.mocks.MockCpsServlet;
import dev.galasa.framework.api.cps.internal.mocks.MockFramework;
import dev.galasa.framework.api.cps.internal.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.cps.internal.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.cps.internal.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.cps.internal.mocks.MockServletOutputStream;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CpsServletTest {

	static final Gson gson = GalasaGsonBuilder.build();

	MockCpsServlet servlet;
	HttpServletRequest req;
	HttpServletResponse resp;

	protected void setServlet(String namespace){
		this.servlet = new MockCpsServlet();
		if (namespace != null){
			IConfigurationPropertyStoreService cpsstore = new MockIConfigurationPropertyStoreService(namespace);
			IFramework framework = new MockFramework(cpsstore);
			this.servlet.setFramework(framework);
		}
	}
	
	protected void setServlet(String path,String namespace, Map<String, String[]> parameterMap){
		setServlet(namespace);
		ServletOutputStream outStream = new MockServletOutputStream();
		PrintWriter writer = new PrintWriter(outStream);
		this.req = new MockHttpServletRequest(parameterMap,path);
		this.resp = new MockHttpServletResponse(writer, outStream);
	}

	protected void setServlet( String path,String namespace, String value){
		setServlet(namespace);
		ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		this.req = new MockHttpServletRequest(path, value);
		this.resp = new MockHttpServletResponse(writer, outStream);
	}

	protected MockCpsServlet getServlet(){
		return this.servlet;
	}

	protected HttpServletRequest getRequest(){
		return this.req;
	}

	protected HttpServletResponse getResponse(){
	return this.resp;
	}
	
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
                if (jsonObject.get("name").getAsString().equals(entry.getKey()) && jsonObject.get("value").getAsString().equals(entry.getValue()) ) {
                    fieldMatches = true;
                }
            }
            assertThat(fieldMatches).isTrue();
        }
    }

	protected boolean checkNewPropertyInNamespace(String propertyName, String propertyValue) throws Exception{
		boolean found = false;
		Map<String,String> properties = this.servlet.getFramework().getConfigurationPropertyService("framework").getAllProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
			String value =entry.getValue().toString();
            if (key.equals(propertyName) && value.equals(propertyValue)){
				found = true;
            }
        }
		return found;
	}

}