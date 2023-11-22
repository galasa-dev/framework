/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import com.google.gson.Gson;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

public class TestCPSProperty {
    
    static final Gson gson = GalasaGsonBuilder.build();

    private String generateExpectedJson(String namespace, String propertyName, String propertyValue){
        return "{\n  \"name\": {\n    \"namespaceName\": \""+namespace+"\",\n    \"simpleName\": \""+propertyName+"\"\n  "+
        "},\n  \"value\": \""+propertyValue+"\"\n}";
    }
    
    @Test
    public void TestGalasaPropertyDefaultApiVersion() throws InternalServletException{
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyFromString() throws InternalServletException{
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        
        //When...
        CPSProperty property = new CPSProperty(fullPropertyName, propertyValue);
        
        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyFromMapEntry() throws InternalServletException{
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        Map.Entry<String, String> entry = Map.entry(fullPropertyName, propertyValue);
        
        //When...
        CPSProperty property = new CPSProperty(entry);
        
        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyCustomApiVersion() throws InternalServletException{
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyInJSONFormat() throws InternalServletException{
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        String expectJson = generateExpectedJson(namespace, propertyName, propertyValue);
        
        //When...
        String jsonString = gson.toJson(property.toJSON());

        //Then...
        assertThat(jsonString).isEqualTo(expectJson);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyFromStringInJSONFormat() throws InternalServletException{
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        CPSProperty property = new CPSProperty(fullPropertyName, propertyValue);
        String expectJson = generateExpectedJson(namespace, propertyName, propertyValue);
        
        //When...
        String jsonString = gson.toJson(property.toJSON());

        //Then...
        assertThat(jsonString).isEqualTo(expectJson);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyFromMapEntryInJSONFormat() throws InternalServletException{
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        Map.Entry<String, String> propertyEntry =  Map.entry(fullPropertyName, propertyValue);
        CPSProperty property = new CPSProperty(propertyEntry);
        String expectJson = generateExpectedJson(namespace, propertyName, propertyValue);
        
        //When...
        String jsonString = gson.toJson(property.toJSON());

        //Then...
        assertThat(jsonString).isEqualTo(expectJson);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyCustomApiVersionInJSONFormat() throws InternalServletException{
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        String expectJson = generateExpectedJson(namespace, propertyName, propertyValue);
        
        //When...
        String jsonString = gson.toJson(property.toJSON());

        //Then...
        assertThat(jsonString).isEqualTo(expectJson);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyNoDataIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            assertThat(property.isPropertyValid()).isFalse();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestGalasaPropertyNoDataDefaultApiVersionIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            assertThat(property.isPropertyValid()).isFalse();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestGalasaPropertyNamespaceOnlyIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            assertThat(property.isPropertyValid()).isFalse();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestGalasaPropertyPartialDataIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "property";
        String propertyValue = null;
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            assertThat(property.isPropertyValid()).isFalse();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }

    @Test
    public void TestGalasaPropertyNoNamespaceIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = "property";
        String propertyValue = "value";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            assertThat(property.isPropertyValid()).isFalse();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","namespace");
    }

    @Test
    public void TestGalasaPropertyNoNameIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "";
        String propertyValue = "value";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            assertThat(property.isPropertyValid()).isFalse();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","name");
    }

    @Test
    public void TestGalasaPropertyNoValueIsInvalid() throws InternalServletException{
        //Given...
        String namespace = "framework";
        String propertyName = "property";
        String propertyValue = "";
        
        //When...
        CPSProperty property = new CPSProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            assertThat(property.isPropertyValid()).isFalse();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","value");
    }
}