/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.resources;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.common.mocks.MockServletOutputStream;

import dev.galasa.framework.api.resources.mocks.MockResourcesServlet;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class ResourcesServletTest extends BaseServletTest {
    
	static final Gson gson = GalasaGsonBuilder.build();

	MockResourcesServlet servlet;
	HttpServletRequest req;
	HttpServletResponse resp;

	protected void setServlet(String namespace){
		this.servlet = new MockResourcesServlet();
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

	protected void setServlet( String path,String namespace, String value, String method){
		setServlet(namespace);
		ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		this.req = new MockHttpServletRequest(path, value, method);
		this.resp = new MockHttpServletResponse(writer, outStream);
	}

	protected MockResourcesServlet getServlet(){
		return this.servlet;
	}

	protected HttpServletRequest getRequest(){
		return this.req;
	}

	protected HttpServletResponse getResponse(){
	return this.resp;
	}

	protected void checkPropertyInNamespace(String namespace, String propertyName, String propertyValue) throws Exception{
		assertThat(checkProperty(namespace, propertyName, propertyValue)).isTrue();
	}
	
	protected void checkPropertyNotInNamespace(String namespace, String propertyName, String propertyValue) throws Exception{
		assertThat(checkProperty(namespace, propertyName, propertyValue)).isFalse();
	}

	protected boolean checkProperty(String namespace, String propertyName, String propertyValue) throws Exception{
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

	protected void checkErrorListContainsError(List<String> errors, String errorMessage){
		boolean match = false;
		for (String error: errors){
			if (error.contains(errorMessage)){
				match = true;
			}
		}
		assertThat(match).isTrue();
	}

	protected String generatePropertyJSON(String namespace, String propertyName, String propertyValue, String apiVersion){
		return "{\n    \"apiVersion\": \""+apiVersion+"\",\n"+
        "    \"kind\": \"GalasaProperty\",\n"+
        "    \"metadata\": {\n"+
        "      \"namespace\": \""+namespace+"\",\n"+
        "      \"name\": \""+propertyName+"\"\n"+
        "    },\n"+
        "    \"data\": {\n"+
        "      \"value\": \""+propertyValue+"\"\n    }\n  }";
	}

	protected String generateArrayJson(String namespace, String propertyName, String propertyValue, String apiVersion){
        return "[\n  "+generatePropertyJSON(namespace, propertyName, propertyValue, apiVersion)+"\n]";
    }

		protected String generateRequestJson(String action, String namespace, String propertyName, String propertyValue, String apiVersion){
        return "{\n \"action\":\""+action+"\", \"data\":"+generateArrayJson(namespace, propertyName, propertyValue, apiVersion)+"\n}";
    }

	protected String generateExpectedJson(Map<String, String> properties){
		String results ="";
		for (Map.Entry<String,String> entry : properties.entrySet()){
			// Key Value namesapce.propertyname value value
			String[] splitName = entry.getKey().split("[.]", 2);
			results += generatePropertyJSON(splitName[0], splitName[1],entry.getValue(),"galasa-dev/v1alpha1");
		} 
        return "[\n  "+results+"\n]";
    }
}
