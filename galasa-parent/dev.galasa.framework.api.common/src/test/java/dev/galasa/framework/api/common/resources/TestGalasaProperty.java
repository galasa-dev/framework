/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import dev.galasa.framework.api.common.InternalServletException;

import static org.assertj.core.api.Assertions.*;

public class TestGalasaProperty {
    
    
    @Test
    public void TestGalasaPropertyDefaultApiVersion() throws InternalServletException{
        //Given...
        String namespace = "mynamespace";
        String propertyName = "new.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        
        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyCustomApiVersion() throws InternalServletException{
        //Given...
        String apiVersion = "randomApi";
        String namespace = "randomnamespace";
        String propertyName = "random.property.name";
        String propertyValue = "randomValue123";
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue, apiVersion);
        
        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo(apiVersion);
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestGalasaPropertyNoDataIsInvalid() throws InternalServletException{
        //Given...
        String apiVersion = null;
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue, apiVersion);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo(null);
        assertThat(property.getNamespace()).isEqualTo(namespace);
        assertThat(property.getName()).isEqualTo(propertyName);
        assertThat(property.getValue()).isEqualTo(propertyValue);
        Throwable thrown = catchThrowable( () -> {
            assertThat(property.isPropertyValid()).isFalse();
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5024","apiVersion");
    }

    @Test
    public void TestGalasaPropertyNoDataDefaultApiVersionIsInvalid() throws InternalServletException{
        //Given...
        String namespace = null;
        String propertyName = null;
        String propertyValue = null;
        
        //When...
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
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
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
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
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
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
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
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
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
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
        GalasaProperty property = new GalasaProperty(namespace, propertyName, propertyValue);
        

        //Then...
        assertThat(property.getKind()).isEqualTo("GalasaProperty");
        assertThat(property.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
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
