/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TestGalasaNamespace {

    @Test
    public void TestNamespaceTypeNormalReturnOkNoURL(){
        //Given...
        String expectedName = "NameSpace1";
        //When...
        GalasaNamespace namespace = new GalasaNamespace(expectedName, GalasaNamespaceType.NORMAL.toString());
        //Then...
        String name = namespace.getName();
        String typeValue = namespace.getNamespaceType();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(typeValue).isEqualTo("normal");
        assertThat(propertiesurl).isEqualTo(null);
    }

    @Test
    public void TestNamespaceTypeNormalReturnOkWithURL(){
        //Given...
        String expectedName = "NameSpace1";
        String expectedUrl = "/NameSpace1/properties";
        //When...
        GalasaNamespace namespace = new GalasaNamespace(expectedName, GalasaNamespaceType.NORMAL.toString(), "/");
        //Then...
        String name = namespace.getName();
        String typeValue = namespace.getNamespaceType();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(typeValue).isEqualTo("normal");
        assertThat(propertiesurl).isEqualTo(expectedUrl);
    }

    @Test
    public void TestNamespaceTypeSecureReturnOkNoURL(){
        //Given...
        String expectedName = "secure";
        //When...
        GalasaNamespace namespace = new GalasaNamespace(expectedName, GalasaNamespaceType.SECURE.toString());
        //Then...
        String name = namespace.getName();
        String typeValue = namespace.getNamespaceType();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(typeValue).isEqualTo("secure");
        assertThat(propertiesurl).isEqualTo(null);
    }

    @Test
    public void TestNamespaceTypeSecureReturnOkWithURL(){
        //Given...
        String expectedName = "secure";
        String expectedUrl = "/secure/properties";
        //When...
        GalasaNamespace namespace = new GalasaNamespace(expectedName, GalasaNamespaceType.SECURE.toString(), "/");
        //Then...
        String name = namespace.getName();
        String typeValue = namespace.getNamespaceType();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(typeValue).isEqualTo("secure");
        assertThat(propertiesurl).isEqualTo(expectedUrl);
    }
}
