/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.regex.Pattern;

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

    /*
     * Regex Path
     */

    @Test
    public void TestPathRegexExpectedPathReturnsTrue(){
        //Given...
        String expectedPath = ResourcesRoute.path;
        String inputPath = "/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexEmptyPathReturnsFalse(){
        //Given...
        String expectedPath = ResourcesRoute.path;
        String inputPath = "";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexRandomPathReturnsFalse(){
        //Given...
        String expectedPath = ResourcesRoute.path;
        String inputPath = "/randomString";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexSpecialCharacterPathReturnsFalse(){
        //Given...
        String expectedPath = ResourcesRoute.path;
        String inputPath = "/?";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexNumberPathReturnsFalse(){
        //Given...
        String expectedPath = ResourcesRoute.path;
        String inputPath = "/3";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexMultipleForwardSlashPathReturnsFalse(){
        //Given...
        String expectedPath = ResourcesRoute.path;
        String inputPath = "//////";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    /*
     * Internal Functions
     */
   
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
        resourcesRoute.processGalasaProperty(propertyJson, "apply");

        //Then...
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyPropertyWithNewNamespaceReturnsOK() throws Exception{
        //Given...
        String namespace = "newnamespace";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet("framework");
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");

        //Then...
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyInvalidPropertyNameReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property1!";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5043E: Invalid property name. Property name 'property1!' much have at least two parts seperated by a . (dot).");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyPropertyNameWithTrailingDotReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name.";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5044E: Invalid property name. Property name 'property.name.' must not end with a . (dot) seperator.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyPropertyNameWithLeadingDotReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = ".property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5041E: Invalid property name. '.property.name' must not start with the '.' character. Allowable first characters are a-z or A-Z.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyBadPropertyNameReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5043E: Invalid property name. Property name 'property' much have at least two parts seperated by a . (dot).");
        checkPropertyNotInNamespace(namespace,propertyname,value);
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
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5040E: Invalid property name. Property name is missing or empty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyMissingPropertyNamespaceReturnsError() throws Exception{
        //Given...
        String namespace = "";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5031E: Invalid namespace. Namespace is empty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyBadNamespaceReturnsError() throws Exception{
        //Given...
        String namespace = "namespace@";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5033E: Invalid namespace name. 'namespace@' must not contain the '@' character. Allowable characters after the first character are a-z, A-Z, 0-9.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyNamespaceWithTrailingDotReturnsError() throws Exception{
        //Given...
        String namespace = "namespace.";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5033E: Invalid namespace name. 'namespace.' must not contain the '.' character. Allowable characters after the first character are a-z, A-Z, 0-9.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyNamespaceWithLeadingDotReturnsError() throws Exception{
        //Given...
        String namespace = ".namespace";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5032E: Invalid namespace name. '.namespace' must not start with the '.' character. Allowable first characters are a-z or A-Z.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
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
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5024E: Error occured because the Galasa Property is invalid.",
            "The 'value' field can not be empty. The field 'value' is mandaotry for the type GalasaProperty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyEmptyFieldsReturnsError() throws Exception{
        //Given...
        String namespace = "";
        String propertyname = "";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = generatePropertyJSON(namespace, propertyname, value, "galasa-dev/v1alpha1");
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(errors.get(0)).contains("GAL5040E: Invalid property name. Property name is missing or empty.");
        assertThat(errors.get(1)).contains("GAL5031E: Invalid namespace. Namespace is empty.");
        assertThat(errors.get(2)).contains("GAL5024E: Error occured because the Galasa Property is invalid. 'The 'value' field can not be empty. The field 'value' is mandaotry for the type GalasaProperty.'");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessGalasaPropertyNoMetadataOrDataReturnsError() throws Exception{
        //Given...
        String namespace = "";
        String propertyname = "";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework framework = servlet.getFramework();
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, framework);
        String jsonString = "{\"apiVersion\": \"galasa-dev/v1alpha1\",\n\"kind\": \"GalasaProperty\",\"metadata\": {},\"data\": {}}";
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(errors.get(0)).contains("GAL5024E: Error occured because the Galasa Property is invalid.",
            "The 'metadata' field can not be empty. The fields 'name' and 'namespace' are mandaotry for the type GalasaProperty.");
        assertThat(errors.get(1)).contains("GAL5024E: Error occured because the Galasa Property is invalid.",
            "The 'data' field can not be empty. The field 'value' is mandaotry for the type GalasaProperty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
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
        resourcesRoute.processGalasaProperty(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5400E: Error occured when trying to execute request ",". Please check your request parameters or report the problem to your Galasa Ecosystem owner.");
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
        checkErrorListContainsError(errors,"GAL5400E: Error occured when trying to execute request ");
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
    public void TestProcessDataArrayCreateWithTwoExistingRecordsJSONReturnsTwoErrors() throws Exception{
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
        assertThat(errors.get(0)).contains("GAL5017E: Error occured when trying to access property 'property.name'. The property does not exist.");
    }

    @Test
    public void TestProcessDataArrayUpdateWithTwoNewRecordsJSONReturnsTwoError() throws Exception{
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
        assertThat(errors.get(0)).contains("GAL5017E: Error occured when trying to access property 'property.name'. The property does not exist.");
        assertThat(errors.get(1)).contains("GAL5017E: Error occured when trying to access property 'property.name.2'. The property does not exist");
    }

    /*
     * POST Requests
     */

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
        assertThat(output).contains("GAL5017E: Error occured when trying to access property 'property.name'. The property does not exist.");
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

    @Test
    public void TestHandlePOSTwithDeleteSingleExistingPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.1";
        String value = "value1";
        String action = "delete";
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
        checkPropertyNotInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithDeleteMultipleExistingPropertiesReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.5";
        String value = "value5";
        String propertynametwo = "property.1";
        String valuetwo = "value1";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "delete";
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
        checkPropertyNotInNamespace(namespace, propertyname, value);
        checkPropertyNotInNamespace(namespace, propertynametwo, valuetwo);
    }

    @Test
    public void TestHandlePOSTwithDeleteSingleNewPropertyReturnsOk() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.10";
        String value = "newvalue";
        String action = "delete";
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
        checkPropertyNotInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithDeleteMultipleNewPropertiesReturnsOk() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.53";
        String value = "value5";
        String propertynametwo = "property.17";
        String valuetwo = "value1";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "delete";
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
        checkPropertyNotInNamespace(namespace, propertyname, value);
        checkPropertyNotInNamespace(namespace, propertynametwo, valuetwo);
    }

    @Test
    public void TestHandlePOSTwithDeleteExistingAndNewPropertiesReturnsOk() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.5";
        String value = "value5";
        String propertynametwo = "property.17";
        String valuetwo = "value1";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "delete";
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
        checkPropertyNotInNamespace(namespace, propertyname, value);
        checkPropertyNotInNamespace(namespace, propertynametwo, valuetwo);
    }

    @Test
    public void TestHandlePOSTwithDeleteSingleExistingPropertyRaisesExceptionReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.1";
        String value = "value1";
        String action = "delete";
		String propertyJSON = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", null, propertyJSON , "POST");
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
        assertThat(output).contains("GAL5030E: Error occured when trying to delete Property 'property.1'. Report the problem to your Galasa Ecosystem owner.");
        checkPropertyNotInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithDeleteMultipleExistingPropertiesRaisesExceptionsReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.5";
        String value = "value5";
        String propertynametwo = "property.1";
        String valuetwo = "value1";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "delete";
        String propertyone = generatePropertyJSON(namespace, propertyname, value, apiVersion);
        String propertytwo = generatePropertyJSON(namespace, propertynametwo, valuetwo, apiVersion);
		String propertyJSON = "{\n \"action\":\""+action+"\", \"data\":["+propertyone+","+propertytwo+"]\n}";
		setServlet("/", null, propertyJSON , "POST");
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
        assertThat(output).contains("GAL5030E: Error occured when trying to delete Property 'property.1'. Report the problem to your Galasa Ecosystem owner.");
        assertThat(output).contains("GAL5030E: Error occured when trying to delete Property 'property.5'. Report the problem to your Galasa Ecosystem owner.");
        checkPropertyNotInNamespace(namespace, propertyname, value);
        checkPropertyNotInNamespace(namespace, propertynametwo, valuetwo);
    }

}
