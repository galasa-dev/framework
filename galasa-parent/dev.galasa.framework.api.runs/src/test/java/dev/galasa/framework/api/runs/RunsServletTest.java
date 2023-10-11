/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIFrameworkRuns;
import dev.galasa.framework.api.common.mocks.MockIRun;
import dev.galasa.framework.api.common.mocks.MockServletOutputStream;
import dev.galasa.framework.api.runs.mocks.MockRunsServlet;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class RunsServletTest extends BaseServletTest {

	static final Gson gson = GalasaGsonBuilder.build();

	MockRunsServlet servlet;
	HttpServletRequest req;
	HttpServletResponse resp;

	protected void setServlet(String path, String groupName, List<IRun> runs){
        this.servlet = new MockRunsServlet();
        ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
        this.resp = new MockHttpServletResponse(writer, outStream);
        this.req = new MockHttpServletRequest(path);
		if (groupName != null){
            IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(groupName, runs);
			IFramework framework = new MockFramework(frameworkRuns);
			this.servlet.setFramework(framework);
		}
	}
	
	protected void setServlet(String path, String groupName, String value, String method){
		setServlet(path, groupName, null);
		this.req = new MockHttpServletRequest(path, value, method);
	}

	protected MockRunsServlet getServlet(){
		return this.servlet;
	}

	protected HttpServletRequest getRequest(){
		return this.req;
	}

	protected HttpServletResponse getResponse(){
	return this.resp;
	}

    

	@Override
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

	protected boolean checkNewPropertyInNamespace(String namespace, String propertyName, String propertyValue) throws Exception{
		boolean found = false;
		Map<String,String> properties = this.servlet.getFramework().getConfigurationPropertyService(namespace).getAllProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
			String value =entry.getValue().toString();
            if (key.equals(namespace+"."+propertyName) && value.equals(propertyValue)){
				found = true;
            }
        }
		return found;
	}

    
}
