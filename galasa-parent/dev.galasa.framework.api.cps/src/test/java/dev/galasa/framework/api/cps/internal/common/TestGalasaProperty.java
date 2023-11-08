/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import org.junit.Test;

import com.google.gson.Gson;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

public class TestGalasaProperty {
    
    static final Gson gson = GalasaGsonBuilder.build();

    private String generateExpectedJson(String namespace, String propertyName, String propertyValue, String apiVersion){
        return "{\n  \"apiVersion\": \""+apiVersion+"\",\n"+
        "  \"kind\": \"GalasaProperty\",\n"+
        "  \"metadata\": {\n"+
        "    \"namespace\": \""+namespace+"\",\n"+
        "    \"name\": \""+propertyName+"\"\n"+
        "  },\n"+
        "  \"data\": {\n"+
        "    \"value\": \""+propertyValue+"\"\n  }\n}";
    }
    
    @Test
    public void TestGalasaPropertyDefaultApiVersion(){
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.apiVersion).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyFromString(){
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        
        //When...
        GalasaProperty property = new GalasaProperty(fullPropertyName, propertyValue);
        
        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.apiVersion).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyFromMapEntry(){
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        Map.Entry<String, String> entry = Map.entry(fullPropertyName, propertyValue);
        
        //When...
        GalasaProperty property = new GalasaProperty(entry);
        
        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.apiVersion).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyCustomApiVersion(){
        //Given...
        String apiVersion = "randomApi";
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue, apiVersion);
        
        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.apiVersion).isEqualTo(apiVersion);
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyInJSONFormat(){
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        String expectJson = generateExpectedJson(namespace, propertyName, propertyValue, "v1alpha1");
        
        //When...
        String jsonString = gson.toJson(property.toJSON());

        //Then...
        assertThat(jsonString).isEqualTo(expectJson);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyFromStringInJSONFormat(){
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        GalasaProperty property = new GalasaProperty(fullPropertyName, propertyValue);
        String expectJson = generateExpectedJson(namespace, propertyName, propertyValue, "v1alpha1");
        
        //When...
        String jsonString = gson.toJson(property.toJSON());

        //Then...
        assertThat(jsonString).isEqualTo(expectJson);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyFromMapEntryInJSONFormat(){
        //Given...
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        String fullPropertyName = namespace+"."+propertyName;
        Map.Entry<String, String> propertyEntry =  Map.entry(fullPropertyName, propertyValue);
        GalasaProperty property = new GalasaProperty(propertyEntry);
        String expectJson = generateExpectedJson(namespace, propertyName, propertyValue, "v1alpha1");
        
        //When...
        String jsonString = gson.toJson(property.toJSON());

        //Then...
        assertThat(jsonString).isEqualTo(expectJson);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyCustomApiVersionInJSONFormat(){
        //Given...
        String apiVersion = "randomApi";
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue, apiVersion);
        String expectJson = generateExpectedJson(namespace, propertyName, propertyValue, apiVersion);
        
        //When...
        String jsonString = gson.toJson(property.toJSON());

        //Then...
        assertThat(jsonString).isEqualTo(expectJson);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyNoDataIsInvalid(){
        //Given...
        String apiVersion = null;
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue, apiVersion);
        

        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.apiVersion).isEqualTo(null);
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isFalse();
    }

    @Test
    public void TestGalasaPropertyNoDataDefaultApiVersionIsInvalid(){
        //Given...
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isFalse();
    }

    @Test
    public void TestGalasaPropertyNamespaceOnlyIsInvalid(){
        //Given...
        String namespace = "framework";
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isFalse();
    }

    @Test
    public void TestGalasaPropertyPartialDataIsInvalid(){
        //Given...
        String namespace = "framework";
        String propertyName = "property";
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isFalse();
    }

    @Test
    public void TestGalasaPropertyNoNamespaceIsInvalid(){
        //Given...
        String namespace = null;
        String propertyName = "property";
        String propertyValue = "value";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isFalse();
    }

    @Test
    public void TestGalasaPropertyNoNameIsInvalid(){
        //Given...
        String namespace = "framework";
        String propertyName = "";
        String propertyValue = "value";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isFalse();
    }

    @Test
    public void TestGalasaPropertyNoValueIsInvalid(){
        //Given...
        String namespace = "framework";
        String propertyName = "property";
        String propertyValue = "";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.kind).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("v1alpha1");
        assertThat(property.metadata.namespace).isEqualTo(namespace);
        assertThat(property.metadata.name).isEqualTo(propertyName);
        assertThat(property.data.value).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isFalse();
    }
}
