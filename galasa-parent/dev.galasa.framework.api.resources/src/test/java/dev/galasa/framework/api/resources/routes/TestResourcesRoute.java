/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.resources.ResourcesServletTest;
import dev.galasa.framework.api.resources.mocks.MockResourcesServlet;
import dev.galasa.framework.spi.IFramework;

public class TestResourcesRoute extends ResourcesServletTest{
   
    
    @Test
    public void TestProcessGalasaPropertyValidPropertyReturnsOK() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            resourcesRoute.processGalasaProperty(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNull();
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyMissingPropertyNameReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            resourcesRoute.processGalasaProperty(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024E: Error occured because the Galasa Property is invalid.",
            "name");
        checkPropertyNotInNamespace(namespace,propertyname,value);;
    }

    @Test
    public void TestProcessGalasaPropertyMissingPropertyNamespaceReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON("", propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            resourcesRoute.processGalasaProperty(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024E: Error occured because the Galasa Property is invalid.",
            "namespace");
        checkPropertyNotInNamespace(namespace,propertyname,value);;
    }

    @Test
    public void TestProcessGalasaPropertyMissingPropertyValueReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            resourcesRoute.processGalasaProperty(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024E: Error occured because the Galasa Property is invalid.",
            "value");
        checkPropertyNotInNamespace(namespace,propertyname,value);;
    }

    @Test
    public void TestProcessGalasaPropertyMissingApiVersionReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            resourcesRoute.processGalasaProperty(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5027E: Error occured because the api version '' is not a supported version. Currently the ecosystem accepts the 'galasa-dev/v1alpha1' api version.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyBadJsonReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = "{\"apiVersion\":\"galasa-dev/v1alpha1\","+namespace+"."+propertyname+":"+value+"}";
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            resourcesRoute.processGalasaProperty(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5000E: Error occured when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessDataArrayBadJsonArrayReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = "[{},{},{}]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
         Throwable thrown = catchThrowable(() -> {
            resourcesRoute.processDataArray(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5000E: Error occured when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }
    
    @Test
    public void TestProcessDataArrayBadJsonReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = "[{\"kind\":\"GalasaProperty\",\"apiVersion\":\"galasa-dev/v1alpha1\","+namespace+"."+propertyname+":"+value+"}]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size() > 0).isTrue();
        checkErrorListContainsError(errors,"GAL5000E: Error occured when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessDataArrayBadKindReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = "[{\"kind\":\"GalasaProperly\",\"apiVersion\":\"v1alpha1\","+namespace+"."+propertyname+":"+value+"}]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size() > 0).isTrue();
        checkErrorListContainsError(errors,"GAL5026E: Error occured because the resource type 'GalasaProperly' is not supported");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessDataArrayCorrectJSONReturnsOK() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generateArrayJson(namespace,propertyname,value,"galasa-dev/v1alpha1");
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(0);
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessDataArrayCreateWithOneExistingRecordJSONReturnsOneError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String propertyNameTwo = "property.1";
        String valueTwo = "random";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString ="["+ generatePropertyJSON(namespace,propertyname,value,"galasa-dev/v1alpha1");
        jsonString = jsonString+","+ generatePropertyJSON(namespace,propertyNameTwo,valueTwo,"galasa-dev/v1alpha1") +"]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "create");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(1);
        checkPropertyInNamespace(namespace,propertyname,value);
        checkPropertyNotInNamespace(namespace,propertyNameTwo,valueTwo);
        assertThat(errors.get(0)).contains("GAL5018E: Error occured when trying to access property 'property.1'. "+
                "The property name provided already exists in the 'framework' namespace.");
    }

    @Test
    public void TestProcessDataArrayCreateWithTwoExistingRecordsJSONReturnsOneError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.1";
        String value = "value";
        String propertyNameTwo = "property.2";
        String valueTwo = "random";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString ="["+ generatePropertyJSON(namespace,propertyname,value,"galasa-dev/v1alpha1");
        jsonString = jsonString+","+ generatePropertyJSON(namespace,propertyNameTwo,valueTwo,"galasa-dev/v1alpha1") +"]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "create");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(2);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        assertThat(errors.get(0)).contains("GAL5018E: Error occured when trying to access property 'property.1'. "+
                "The property name provided already exists in the 'framework' namespace.");
        assertThat(errors.get(1)).contains("GAL5018E: Error occured when trying to access property 'property.2'. "+
                "The property name provided already exists in the 'framework' namespace.");
    }

    @Test
    public void TestProcessDataArrayUpdateWithOneNewRecordJSONReturnsOneError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String propertyNameTwo = "property.1";
        String valueTwo = "random";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString ="["+ generatePropertyJSON(namespace,propertyname,value,"galasa-dev/v1alpha1");
        jsonString = jsonString+","+ generatePropertyJSON(namespace,propertyNameTwo,valueTwo,"galasa-dev/v1alpha1") +"]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "update");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(1);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        checkPropertyInNamespace(namespace,propertyNameTwo,valueTwo);
        assertThat(errors.get(0)).contains("GAL5017E: Error occured when trying to access property 'property.name'. The property name provided is invalid.");
    }

    @Test
    public void TestProcessDataArrayUpdateWithTwoNewRecordsJSONReturnsOneError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String propertyNameTwo = "property.name.2";
        String valueTwo = "random";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString ="["+ generatePropertyJSON(namespace,propertyname,value,"galasa-dev/v1alpha1");
        jsonString = jsonString+","+ generatePropertyJSON(namespace,propertyNameTwo,valueTwo,"galasa-dev/v1alpha1") +"]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "update");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(2);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        assertThat(errors.get(0)).contains("GAL5017E: Error occured when trying to access property 'property.name'. The property name provided is invalid.");
        assertThat(errors.get(1)).contains("GAL5017E: Error occured when trying to access property 'property.name.2'. The property name provided is invalid.");
    }

    @Test
    public void TestProcessRequestApplyActionReturnsOK() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "apply";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generateRequestJson(action, namespace,propertyname,value,"galasa-dev/v1alpha1");

        //When...
        resourcesRoute.processRequest(jsonString);
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(0);
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessRequestCreateActionReturnsOK() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "create";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generateRequestJson(action, namespace,propertyname,value,"galasa-dev/v1alpha1");

        //When...
        resourcesRoute.processRequest(jsonString);
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(0);
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessRequestUpdateActionReturnsOK() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.1";
        String value = "value";
        String action = "apply";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generateRequestJson(action, namespace,propertyname,value,"galasa-dev/v1alpha1");

        //When...
        resourcesRoute.processRequest(jsonString);
         List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(0);
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessRequestBadActionReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "BadAction";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generateRequestJson(action, namespace,propertyname,value,"galasa-dev/v1alpha1");

        //When...
        Throwable thrown = catchThrowable(() -> {
          resourcesRoute.processRequest(jsonString);
        });

        //Then...
        assertThat(thrown).isNotNull();
        String message = thrown.getMessage();
        checkErrorStructure(message, 
            5025,
            "GAL5025E: Error occurred when trying to apply resources. Action 'badaction' supplied is not supported. Supported actions are: create, apply and update.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestHandlePOSTwithApplySingleNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "apply";
		String propertyJSON = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        checkPropertyInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithCreateSingleNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "create";
		String propertyJSON = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        checkPropertyInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithUpdateSingleNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "update";
		String propertyJSON = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(400);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).contains("The property name provided is invalid.");
        checkPropertyNotInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithApplyMultipleNewPropertiesReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.6";
        String value = "value6";
        String propertynametwo = "property.name";
        String valuetwo = "value";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "apply";
        String propertyone = generatePropertyJSON(namespace, propertyname, value, apiVersion);
        String propertytwo = generatePropertyJSON(namespace, propertynametwo, valuetwo, apiVersion);
		String propertyJSON = "{\n \"action\":\""+action+"\", \"data\":["+propertyone+","+propertytwo+"]\n}";
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        checkPropertyInNamespace(namespace, propertyname, value);
        checkPropertyInNamespace(namespace, propertynametwo, valuetwo);
    }

    @Test
    public void TestHandlePOSTwithApplySingleExistingPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.1";
        String value = "newvalue";
        String action = "apply";
		String propertyJSON = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        checkPropertyInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithCreateSingleExistingPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.1";
        String value = "newvalue";
        String action = "create";
		String propertyJSON = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(400);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(output).contains("The property name provided already exists");
        checkPropertyNotInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithUpdateSingleExistingPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.1";
        String value = "newvalue";
        String action = "update";
		String propertyJSON = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();	

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        checkPropertyInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithApplyMultipleExistingPropertiesReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.5";
        String value = "value6";
        String propertynametwo = "property.1";
        String valuetwo = "newvalue";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "apply";
        String propertyone = generatePropertyJSON(namespace, propertyname, value, apiVersion);
        String propertytwo = generatePropertyJSON(namespace, propertynametwo, valuetwo, apiVersion);
		String propertyJSON = "{\n \"action\":\""+action+"\", \"data\":["+propertyone+","+propertytwo+"]\n}";
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        checkPropertyInNamespace(namespace, propertyname, value);
        checkPropertyInNamespace(namespace, propertynametwo, valuetwo);
    }

    @Test
    public void TestHandlePOSTwithApplyMultipleExistingAndNewPropertiesReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "new.property";
        String value = "value6";
        String propertynametwo = "property.1";
        String valuetwo = "newvalue";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "apply";
        String propertyone = generatePropertyJSON(namespace, propertyname, value, apiVersion);
        String propertytwo = generatePropertyJSON(namespace, propertynametwo, valuetwo, apiVersion);
		String propertyJSON = "{\n \"action\":\""+action+"\", \"data\":["+propertyone+","+propertytwo+"]\n}";
		setServlet("/", namespace, propertyJSON , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("*");
        checkPropertyInNamespace(namespace, propertyname, value);
        checkPropertyInNamespace(namespace, propertynametwo, valuetwo);
    }

}
