/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.cps.internal.CpsServletTest;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.*;
import dev.galasa.framework.api.common.resources.CPSProperty;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TestCPSRoute extends CpsServletTest {

    public class CPSRouteTest extends CPSRoute {
        public CPSRouteTest(){
        super(new ResponseBuilder(), "",new MockFramework(new MockIConfigurationPropertyStoreService("framework")));
        }
    }
    
    @Test
    public void TestCreateGalasaPropertyReturnsSuccess(){
        //Given...
        
        CPSRouteTest cpsRoute = new CPSRouteTest();
        CPSProperty property = new CPSProperty("framework", "property.10", "value");
        boolean updateProperty = false;

        //When...
        Throwable thrown = catchThrowable( () -> {
           cpsRoute.propertyUtility.setProperty(property, updateProperty);
        });

        //Then...
        assertThat(thrown).isNull();
    }
    
    @Test
    public void TestCreateGalasaPropertyWherePropertyAlreadyExistsReturnsError(){
        //Given...
        CPSRouteTest cpsRoute = new CPSRouteTest();
        CPSProperty property = new CPSProperty("framework","property.1", "value");
        boolean updateProperty = false;

        //When...
        Throwable thrown = catchThrowable( () -> {
           cpsRoute.propertyUtility.setProperty(property, updateProperty);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5018");
    }

    @Test
    public void TestUpdateGalasaPropertyReturnsSuccess(){
        //Given...
        CPSRouteTest cpsRoute = new CPSRouteTest();
        CPSProperty property = new CPSProperty("framework","property.1", "value");
        boolean updateProperty = true;

        //When...
        Throwable thrown = catchThrowable( () -> {
           cpsRoute.propertyUtility.setProperty(property, updateProperty);
        });

        //Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestUpdateGalasaPropertyWherePropertyDoesNotExistReturnsError(){
        //Given...
        CPSRouteTest cpsRoute = new CPSRouteTest();
        CPSProperty property = new CPSProperty("framework","property.10", "value");
        boolean updateProperty = true;

        //When...
        Throwable thrown = catchThrowable( () -> {
           cpsRoute.propertyUtility.setProperty(property, updateProperty);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5017");
    }

    @Test
    public void TestUpdateGalasaPropertyWhereGalasaPropertyisNullReturnsError(){
        //Given...
        CPSRouteTest cpsRoute = new CPSRouteTest();
        CPSProperty property = null;
        boolean updateProperty = true;

        //When...
        Throwable thrown = catchThrowable( () -> {
           cpsRoute.propertyUtility.setProperty(property, updateProperty);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(NullPointerException.class);
    }

    @Test
    public void TestGetPropertyNameFromValidUrlWithCorrectURLStructureReturnsOk() throws InternalServletException{
        // Given...
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String pathInfo = "/framework/properties/property1";

        //When..
        String actualName = cpsRoute.getPropertyNameFromURL(pathInfo);

        // Then...
        assertThat("property1").isEqualTo(actualName);
    }
    
    @Test 
    public void TestGetPropertyNameFromUrlWithCorrectURLStructureReturnsError(){
        //Given...
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String pathInfo = "/framework/properties";

          //When...
        Throwable thrown = catchThrowable( () -> {
           cpsRoute.getPropertyNameFromURL(pathInfo);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5000");
    }

    @Test 
    public void TestGetPropertyNamespaceWithCorrectUrlStructureReturnsOk() throws InternalServletException{
        //Given...
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String pathInfo = "/framework/properties?prefix=<prefix>&suffix=<suffix>";
        String expectedNamespace = "framework";
             
        String actualNamespace = cpsRoute.getNamespaceFromURL(pathInfo);
        
        //Then
        assertThat(expectedNamespace).isEqualTo(actualNamespace);
    }

    @Test 
    public void TestGetPropertyNamespaceFromWrongUrlStructureReturnsError(){
        //Given...
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String pathInfo = "/";

        //When...
        Throwable thrown = catchThrowable( () -> {
           cpsRoute.getNamespaceFromURL(pathInfo);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5000");
    }

    @Test 
    public void TestGetGalasaPropertyFromJsonStringReturnsCorrectGalasaProperty() throws InternalServletException{
        //Given
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String namespace = "namethisspace";
        String name = "house";
        String value = "building";
        String jsonString = generatePropertyJSON(namespace, name, value, "galasa-dev/v1alpha1");

        //When...
        CPSProperty property = cpsRoute.propertyUtility.getGalasaPropertyfromJsonString(jsonString);

        //Then...
        assertThat(property.metadata.name).isEqualTo(name);
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.data.value).isEqualTo(value);
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.apiVersion).isEqualTo("galasa-dev/v1alpha1");
    }

    @Test 
    public void TestGetGalasaPropertyFromJsonStringNullNamespaceReturnsError() {
        //Given
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String name = "doughnuts";
        String value = "custard";
        String jsonString = "{\n    \"apiVersion\": \"v1alpha1\",\n"+
        "    \"kind\": \"GalasaProperty\",\n"+
        "    \"metadata\": {\n"+
        "      \"namespace\": ,\n"+
        "      \"name\": \""+name+"\"\n"+
        "    },\n"+
        "    \"data\": {\n"+
        "      \"value\": \""+value+"\"\n    }\n  }";

        //When...
        Throwable thrown = catchThrowable( () -> {
            cpsRoute.propertyUtility.getGalasaPropertyfromJsonString(jsonString);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5023");
    }

    @Test 
    public void TestGetGalasaPropertyFromJsonStringNullNameReturnsError() {
        //Given
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String namespace = "gingernuts";
        String value = "oatmeal";
        String jsonString = "{\n    \"apiVersion\": \"v1alpha1\",\n"+
        "    \"kind\": \"GalasaProperty\",\n"+
        "    \"metadata\": {\n"+
        "      \"namespace\": \""+namespace+"\",\n"+
        "      \"name\": \n"+
        "    },\n"+
        "    \"data\": {\n"+
        "      \"value\": \""+value+"\"\n    }\n  }";

        //When...
        Throwable thrown = catchThrowable( () -> {
            cpsRoute.propertyUtility.getGalasaPropertyfromJsonString(jsonString);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5023");
    }

    @Test 
    public void TestGetGalasaPropertyFromJsonStringNullValueReturnsError() {
        //Given
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String namespace = "digestives";
        String name = "walkers";
        String jsonString = "{\n    \"apiVersion\": \"v1alpha1\",\n"+
        "    \"kind\": \"GalasaProperty\",\n"+
        "    \"metadata\": {\n"+
        "      \"namespace\": \""+namespace+"\",\n"+
        "      \"name\": \""+name+"\"\n"+
        "    },\n"+
        "    \"data\": {\n"+
        "      \"value\": \n    }\n  }";

        //When...
        Throwable thrown = catchThrowable( () -> {
            cpsRoute.propertyUtility.getGalasaPropertyfromJsonString(jsonString);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5023");
    }

    @Test 
    public void TestGetGalasaPropertyFromEmptyJsonStringReturnsError() {
        //Given
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String jsonString = "";

        //When...
        Throwable thrown = catchThrowable( () -> {
            cpsRoute.propertyUtility.getGalasaPropertyfromJsonString(jsonString);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5023");
    }

    @Test 
    public void TestGetGalasaPropertyFromBadJsonStringReturnsError() {
        //Given
        CPSRouteTest cpsRoute = new CPSRouteTest();
        String jsonString = "{\"name\":\"cookies&cream\", \"value\":\"£12.50\"}";

        //When...
        Throwable thrown = catchThrowable( () -> {
            cpsRoute.propertyUtility.getGalasaPropertyfromJsonString(jsonString);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5023");
    }
}