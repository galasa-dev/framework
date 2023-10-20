/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TestNamespace {

    @Test
    public void TestNamespaceTypeNormalReturnOkNoURL(){
        //Given...
        String expectedName = "NameSpace1";
        //When...
        Namespace namespace = new Namespace(expectedName, NamespaceType.NORMAL.toString());
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
        Namespace namespace = new Namespace(expectedName, NamespaceType.NORMAL.toString(), "/");
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
        Namespace namespace = new Namespace(expectedName, NamespaceType.SECURE.toString());
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
        Namespace namespace = new Namespace(expectedName, NamespaceType.SECURE.toString(), "/");
        //Then...
        String name = namespace.getName();
        String typeValue = namespace.getNamespaceType();
        String propertiesurl = namespace.getPropertiesUrl();
        assertThat(name).isEqualTo(expectedName);
        assertThat(typeValue).isEqualTo("secure");
        assertThat(propertiesurl).isEqualTo(expectedUrl);
    }
}
