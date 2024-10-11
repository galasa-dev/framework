/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.resources.ResourcesServletTest;
import dev.galasa.framework.api.resources.mocks.MockResourcesServlet;
import dev.galasa.framework.api.resources.processors.GalasaPropertyProcessor;
import dev.galasa.framework.spi.utils.GalasaGson;

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        propertyProcessor.processResource(propertyJson, "apply");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        propertyProcessor.processResource(propertyJson, "apply");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");


        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5043E: Invalid property name. Property name 'property1!' much have at least two parts separated by a . (dot).");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5044E: Invalid property name. Property name 'property.name.' must not end with a . (dot) separator.");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5043E: Invalid property name. Property name 'property' much have at least two parts separated by a . (dot).");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5024E: Error occurred because the Galasa Property is invalid.",
            "The 'value' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(errors.get(0)).contains("GAL5040E: Invalid property name. Property name is missing or empty.");
        assertThat(errors.get(1)).contains("GAL5031E: Invalid namespace. Namespace is empty.");
        assertThat(errors.get(2)).contains("GAL5024E: Error occurred because the Galasa Property is invalid. 'The 'value' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.'");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        String jsonString = "{\"apiVersion\": \"galasa-dev/v1alpha1\",\n\"kind\": \"GalasaProperty\",\"metadata\": {},\"data\": {}}";
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, "apply");

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(errors.get(0)).contains("GAL5024E: Error occurred because the Galasa Property is invalid.",
            "The 'metadata' field cannot be empty. The fields 'name' and 'namespace' are mandatory for the type GalasaProperty.");
        assertThat(errors.get(1)).contains("GAL5024E: Error occurred because the Galasa Property is invalid.",
            "The 'data' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "");

        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyProcessor.processResource(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5027E: Error occurred. The field apiVersion in the request body is invalid. The value '' is not a supported version." +
            " Currently the ecosystem accepts the 'galasa-dev/v1alpha1' api version. This could indicate a mis-match between client and server levels." +
            " Please check the level with your Ecosystem administrator. You may have to upgrade/downgrade your client program.");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        String jsonString = "{\"apiVersion\":\"galasa-dev/v1alpha1\","+namespace+"."+propertyname+":"+value+"}";
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyProcessor.processResource(propertyJson, "apply");
        });

        //Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(
            thrown.getMessage(),
            5069,
            "GAL5069E",
            "Invalid request body provided. One or more of the following mandatory fields are missing",
            "[apiVersion, metadata, data]"
        );
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString = "[{},{},{}]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(3);
        checkErrorListContainsError(errors,"GAL5068E: Error occurred. The JSON element for a resource can not be empty. Please check the request format, or check with your Ecosystem administrator.");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString = "[{\"kind\":\"GalasaProperty\",\"apiVersion\":\"galasa-dev/v1alpha1\","+namespace+"."+propertyname+":"+value+"}]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(1);
        checkErrorListContainsError(errors,"GAL5069E: Invalid request body provided. One or more of the following mandatory fields are missing from the request body");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString = "[{\"kind\":\"GalasaProperly\",\"apiVersion\":\"v1alpha1\","+namespace+"."+propertyname+":"+value+"}]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(1);
        checkErrorListContainsError(errors,"GAL5026E: Error occurred. The field kind in the request body is invalid. The value 'GalasaProperly' is not supported." +
            " This could indicate a mis-match between client and server levels. Please check the level with your Ecosystem administrator." +
            " You may have to upgrade/downgrade your client program.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessDataArrayNullJsonObjectReturnsError() throws Exception{
        //Given...
        String namespace = "framework";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString = "[null]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(1);
        checkErrorListContainsError(errors,"GAL5067E: Error occurred. A 'NULL' value is not a valid resource. Please check the request format, or check with your Ecosystem administrator.");
    }

    @Test
    public void TestProcessDataArrayCorrectJSONReturnsOK() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        JsonArray propertyJson = generatePropertyArrayJson(namespace,propertyname,value,"galasa-dev/v1alpha1");

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(0);
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestProcessDataArrayThreeBadJsonReturnsErrors() throws Exception{
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString = "[null, {\"kind\":\"GalasaProperty\",\"apiVersion\":\"galasa-dev/v1alpha1\","+namespace+"."+propertyname+":"+value+"},"+
            "{\"kind\":\"GalasaProperly\",\"apiVersion\":\"v1alpha1\","+namespace+"."+propertyname+":"+value+"},{}]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "apply");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(4);
        checkErrorListContainsError(errors,"GAL5067E: Error occurred. A 'NULL' value is not a valid resource. Please check the request format, or check with your Ecosystem administrator.");
        checkErrorListContainsError(errors,"GAL5069E: Invalid request body provided. One or more of the following mandatory fields are missing from the request body");
        checkErrorListContainsError(errors,"GAL5026E: Error occurred. The field kind in the request body is invalid. The value 'GalasaProperly' is not supported." +
            " This could indicate a mis-match between client and server levels. Please check the level with your Ecosystem administrator." +
            " You may have to upgrade/downgrade your client program.");
        checkErrorListContainsError(errors,"GAL5068E: Error occurred. The JSON element for a resource can not be empty. Please check the request format, or check with your Ecosystem administrator.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString ="["+ generatePropertyJson(namespace,propertyname,value,"galasa-dev/v1alpha1");
        jsonString = jsonString+","+ generatePropertyJson(namespace,propertyNameTwo,valueTwo,"galasa-dev/v1alpha1") +"]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "create");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(1);
        checkPropertyInNamespace(namespace,propertyname,value);
        checkPropertyNotInNamespace(namespace,propertyNameTwo,valueTwo);
        assertThat(errors.get(0)).contains("GAL5018E: Error occurred when trying to access property 'property.1'. "+
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString ="["+ generatePropertyJson(namespace,propertyname,value,"galasa-dev/v1alpha1");
        jsonString = jsonString+","+ generatePropertyJson(namespace,propertyNameTwo,valueTwo,"galasa-dev/v1alpha1") +"]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "create");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(2);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        assertThat(errors.get(0)).contains("GAL5018E: Error occurred when trying to access property 'property.1'. "+
                "The property name provided already exists in the 'framework' namespace.");
        assertThat(errors.get(1)).contains("GAL5018E: Error occurred when trying to access property 'property.2'. "+
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString ="["+ generatePropertyJson(namespace,propertyname,value,"galasa-dev/v1alpha1");
        jsonString = jsonString+","+ generatePropertyJson(namespace,propertyNameTwo,valueTwo,"galasa-dev/v1alpha1") +"]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "update");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(1);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        checkPropertyInNamespace(namespace,propertyNameTwo,valueTwo);
        assertThat(errors.get(0)).contains("GAL5017E: Error occurred when trying to access property 'property.name'. The property does not exist.");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        String jsonString ="["+ generatePropertyJson(namespace,propertyname,value,"galasa-dev/v1alpha1");
        jsonString = jsonString+","+ generatePropertyJson(namespace,propertyNameTwo,valueTwo,"galasa-dev/v1alpha1") +"]";
        JsonArray propertyJson = JsonParser.parseString(jsonString).getAsJsonArray();

        //When...
        resourcesRoute.processDataArray(propertyJson, "update");
        List<String> errors = resourcesRoute.errors;

        //Then...
        assertThat(errors.size()).isEqualTo(2);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        checkPropertyNotInNamespace(namespace,propertyname,value);
        assertThat(errors.get(0)).contains("GAL5017E: Error occurred when trying to access property 'property.name'. The property does not exist.");
        assertThat(errors.get(1)).contains("GAL5017E: Error occurred when trying to access property 'property.name.2'. The property does not exist");
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        JsonObject requestJson = generateRequestJson(action, namespace,propertyname,value,"galasa-dev/v1alpha1");

        //When...
        resourcesRoute.processRequest(requestJson);
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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        JsonObject jsonString = generateRequestJson(action, namespace,propertyname,value,"galasa-dev/v1alpha1");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        JsonObject jsonString = generateRequestJson(action, namespace,propertyname,value,"galasa-dev/v1alpha1");

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
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);
        JsonObject jsonString = generateRequestJson(action, namespace,propertyname,value,"galasa-dev/v1alpha1");

        //When...
        Throwable thrown = catchThrowable(() -> {
          resourcesRoute.processRequest(jsonString);
        });

        //Then...
        assertThat(thrown).isNotNull();
        String message = thrown.getMessage();
        checkErrorStructure(message, 
            5025,
            "GAL5025E: Error occurred. The field action in the request body is invalid. The action value'badaction' supplied is not supported." +
                " Supported actions are: create, apply and update. This could indicate a mis-match between client and server levels." +
                " Please check the level with your Ecosystem administrator. You may have to upgrade/downgrade your client program.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void TestHandlePOSTwithApplySingleNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "apply";
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJson , "POST");
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
        checkPropertyInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithCreateSingleNewPropertyReturnsSuccess() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "create";
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJson , "POST");
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
        checkPropertyInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithUpdateSingleNewPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        String action = "update";
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJson , "POST");
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
        assertThat(output).contains("GAL5017E: Error occurred when trying to access property 'property.name'. The property does not exist.");
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
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);
		JsonObject propertyJson = generateRequestJson(action, List.of(propertyone, propertytwo));
		setServlet("/", namespace, propertyJson , "POST");
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
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJson , "POST");
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
        checkPropertyInNamespace(namespace, propertyname, value);
    }

    @Test
    public void TestHandlePOSTwithCreateSingleExistingPropertyReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.1";
        String value = "newvalue";
        String action = "create";
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJson , "POST");
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
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJson , "POST");
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
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);
		JsonObject propertyJson = generateRequestJson(action, List.of(propertyone, propertytwo));
		setServlet("/", namespace, propertyJson , "POST");
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
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);
		JsonObject propertyJson = generateRequestJson(action, List.of(propertyone, propertytwo));
		setServlet("/", namespace, propertyJson , "POST");
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
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJson , "POST");
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
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);
		JsonObject propertyJson = generateRequestJson(action, List.of(propertyone, propertytwo));
		setServlet("/", namespace, propertyJson , "POST");
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
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", namespace, propertyJson , "POST");
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
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);
		JsonObject propertyJson = generateRequestJson(action, List.of(propertyone, propertytwo));
		setServlet("/", namespace, propertyJson , "POST");
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
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);
		JsonObject propertyJson = generateRequestJson(action, List.of(propertyone, propertytwo));
		setServlet("/", namespace, propertyJson , "POST");
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
		JsonObject propertyJson = generateRequestJson(action, namespace, propertyname,value,"galasa-dev/v1alpha1");
		setServlet("/", null, propertyJson , "POST");
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
        assertThat(output).contains("GAL5030E: Error occurred when trying to delete Property 'property.1'. Report the problem to your Galasa Ecosystem owner.");
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
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);
		JsonObject propertyJson = generateRequestJson(action, List.of(propertyone, propertytwo));
		setServlet("/", null, propertyJson , "POST");
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
        assertThat(output).contains("GAL5030E: Error occurred when trying to delete Property 'property.1'. Report the problem to your Galasa Ecosystem owner.");
        assertThat(output).contains("GAL5030E: Error occurred when trying to delete Property 'property.5'. Report the problem to your Galasa Ecosystem owner.");
        checkPropertyNotInNamespace(namespace, propertyname, value);
        checkPropertyNotInNamespace(namespace, propertynametwo, valuetwo);
    }

    @Test
    public void TestHandlePOSTwithApplyMultipleExistingAndNewAndNullPropertiesReturnsError() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "new.property";
        String value = "value6";
        String propertynametwo = "property.1";
        String valuetwo = "newvalue";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "apply";
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);

        List<JsonObject> propertyList = new ArrayList<>();
        propertyList.add(null);
        propertyList.add(propertyone);
        propertyList.add(propertytwo);

		JsonObject propertyJson = generateRequestJson(action, propertyList);
		setServlet("/", namespace, propertyJson , "POST");
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
        assertThat(output).contains("GAL5067E: Error occurred. A 'NULL' value is not a valid resource. Please check the request format, or check with your Ecosystem administrator.");
        checkPropertyInNamespace(namespace, propertyname, value);
        checkPropertyInNamespace(namespace, propertynametwo, valuetwo);
    }

    @Test
    public void TestHandlePOSTwithNoDataReturnsOK() throws Exception {
        // Given...
		JsonObject propertyJson = generateRequestJson("apply", new ArrayList<>());
		setServlet("/", "framework", propertyJson , "POST");
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
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json"); 
        assertThat(output).isEqualTo("");
    }

    @Test
    public void TestGetErrorsAsJsonReturnsJsonString() throws Exception{
        // Given...
        List<String> errors = new ArrayList<String>();
        errors.add("{\"error_code\":5030,\"error_message\":\"GAL5030E: Error occurred when trying to delete Property 'property.5'. Report the problem to your Galasa Ecosystem owner.\"}");
        errors.add("{\"error_code\":5030,\"error_message\":\"GAL5030E: Error occurred when trying to delete Property 'property.1'. Report the problem to your Galasa Ecosystem owner.\"}");
        setServlet("framework");
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        ResourcesRoute resourcesRoute = new ResourcesRoute(null, cps, null);

        // When...
        String json = resourcesRoute.getErrorsAsJson(errors);
        
        // Then...
        // Generate Expected JSON
        JsonArray expectedJsonArray = new JsonArray();
        JsonObject errorProp5 = new JsonObject();
        errorProp5.addProperty("error_code" , 5030);
        errorProp5.addProperty("error_message" , "GAL5030E: Error occurred when trying to delete Property 'property.5'. Report the problem to your Galasa Ecosystem owner.");
        JsonObject errorProp1 = new JsonObject();
        errorProp1.addProperty("error_code" , 5030);
        errorProp1.addProperty("error_message" , "GAL5030E: Error occurred when trying to delete Property 'property.1'. Report the problem to your Galasa Ecosystem owner.");
        expectedJsonArray.add(errorProp5);
        expectedJsonArray.add(errorProp1);
        GalasaGson gson = new GalasaGson();
        String expectedJson =gson.toJson(expectedJsonArray);
        assertThat(json).isEqualTo(expectedJson);
    }


    @Test
    public void TestHandlePOSTwithDeleteMultipleExistingPropertiesRaisesExceptionsReturnsErrorArray() throws Exception {
        // Given...
		String namespace = "framework";
        String propertyname = "property.5";
        String value = "value5";
        String propertynametwo = "property.1";
        String valuetwo = "value1";
        String apiVersion = "galasa-dev/v1alpha1";
        String action = "delete";
        JsonObject propertyone = generatePropertyJson(namespace, propertyname, value, apiVersion);
        JsonObject propertytwo = generatePropertyJson(namespace, propertynametwo, valuetwo, apiVersion);
		JsonObject propertyJson = generateRequestJson(action, List.of(propertyone, propertytwo));
        Map<String, String> headers = new HashMap<String,String>();
        headers.put("Accept", "application/xml");
		setServlet("/", "framework", propertyJson , "POST", headers);
		setServlet("/", null, propertyJson , "POST");
		MockResourcesServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        // Generate Expected JSON
        JsonArray expectedJsonArray = new JsonArray();
        JsonObject errorProp5 = new JsonObject();
        errorProp5.addProperty("error_code" , 5030);
        errorProp5.addProperty("error_message" , "GAL5030E: Error occurred when trying to delete Property 'property.5'. Report the problem to your Galasa Ecosystem owner.");
        JsonObject errorProp1 = new JsonObject();
        errorProp1.addProperty("error_code" , 5030);
        errorProp1.addProperty("error_message" , "GAL5030E: Error occurred when trying to delete Property 'property.1'. Report the problem to your Galasa Ecosystem owner.");
        expectedJsonArray.add(errorProp5);
        expectedJsonArray.add(errorProp1);
        GalasaGson gson = new GalasaGson();
        String expectedJson =gson.toJson(expectedJsonArray);
        
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(400);
		assertThat(resp.getContentType()).isEqualTo("application/json"); 
        assertThat(output).isEqualTo(expectedJson);
        checkPropertyNotInNamespace(namespace, propertyname, value);
        checkPropertyNotInNamespace(namespace, propertynametwo, valuetwo);
    }
}
