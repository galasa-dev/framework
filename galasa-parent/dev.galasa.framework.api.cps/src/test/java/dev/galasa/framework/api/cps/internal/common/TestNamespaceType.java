/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TestNamespaceType {

    @Test
    public void TestNamespaceTypeNormalReturnNormal(){
        //Given...
        NamespaceType namespaceType = NamespaceType.NORMAL;
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("normal");
    }

    @Test
    public void TestNamespaceTypeSecureReturnSecure(){
        //Given...
        NamespaceType namespaceType = NamespaceType.SECURE;
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("secure");
    }

    @Test
    public void TestNamespaceTypeGetFromStringNormalReturnNormal(){
        //Given...
        NamespaceType namespaceType = NamespaceType.getfromString("NoRmal");
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("normal");
    }

    @Test
    public void TestNamespaceTypeGetFromStringSecureReturnSecure(){
        //Given...
        NamespaceType namespaceType = NamespaceType.getfromString("SecUre");
        //When...
        String typeValue = namespaceType.toString();
        //Then...
        assertThat(typeValue).isEqualTo("secure");
    }
}
