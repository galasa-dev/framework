/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TestNamespaceType {

    @Test
    public void TestNamespaceTypeNormalReturnNormal(){
        //Given...
        GalasaNamespaceType namespaceType = GalasaNamespaceType.NORMAL;
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("normal");
    }

    @Test
    public void TestNamespaceTypeSecureReturnSecure(){
        //Given...
        GalasaNamespaceType namespaceType = GalasaNamespaceType.SECURE;
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("secure");
    }

    @Test
    public void TestNamespaceTypeGetFromStringNormalReturnNormal(){
        //Given...
        GalasaNamespaceType namespaceType = GalasaNamespaceType.getfromString("NoRmal");
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("normal");
    }

    @Test
    public void TestNamespaceTypeGetFromStringSecureReturnSecure(){
        //Given...
        GalasaNamespaceType namespaceType = GalasaNamespaceType.getfromString("SecUre");
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("secure");
    }
}
