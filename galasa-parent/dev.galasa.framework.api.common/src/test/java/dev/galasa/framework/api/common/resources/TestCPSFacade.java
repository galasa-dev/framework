/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import java.util.*;

import org.junit.Test;

import dev.galasa.framework.api.common.mocks.*;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

import static org.assertj.core.api.Assertions.*;



public class TestCPSFacade {

    @Test
    public void TestNamespacesListContainsFramework() throws ConfigurationPropertyStoreException{

        IConfigurationPropertyStoreService cps = new MockConfigurationPropertyStoreService();

        IFramework mockFramework = new MockFramework(cps);
        CPSFacade facade = new CPSFacade(mockFramework);
        Map<String,CPSNamespace> spaces = facade.getNamespaces();

        assertThat(spaces).containsKeys("framework");
        CPSNamespace ns = spaces.get("framework");
        assertThat(ns.getName()).isEqualTo("framework");
        assertThat(ns.getVisibility()).isEqualTo(Visibility.NORMAL);
    }

    @Test
    public void TestNamespacesListContainsDSS() throws ConfigurationPropertyStoreException{

        IConfigurationPropertyStoreService cps = new MockConfigurationPropertyStoreService();
        IFramework mockFramework = new MockFramework(cps);
        CPSFacade facade = new CPSFacade(mockFramework);
        Map<String,CPSNamespace> spaces = facade.getNamespaces();


        assertThat(spaces).containsKeys("dss");
        CPSNamespace ns = spaces.get("dss");
        assertThat(ns.getName()).isEqualTo("dss");
        assertThat(ns.getVisibility()).isEqualTo(Visibility.HIDDEN);
    }

    @Test
    public void TestNamespacesListContainsDex() throws ConfigurationPropertyStoreException{

        IConfigurationPropertyStoreService cps = new MockConfigurationPropertyStoreService();
        IFramework mockFramework = new MockFramework(cps);
        CPSFacade facade = new CPSFacade(mockFramework);
        Map<String,CPSNamespace> spaces = facade.getNamespaces();

        assertThat(spaces).containsKeys("dex");
        CPSNamespace ns = spaces.get("dex");
        assertThat(ns.getName()).isEqualTo("dex");
        assertThat(ns.getVisibility()).isEqualTo(Visibility.HIDDEN);
    }

    @Test
    public void TestNamespacesListContainsSecure() throws ConfigurationPropertyStoreException{

        IConfigurationPropertyStoreService cps = new MockConfigurationPropertyStoreService();
        IFramework mockFramework = new MockFramework(cps);
        CPSFacade facade = new CPSFacade(mockFramework);
        Map<String,CPSNamespace> spaces = facade.getNamespaces();

        assertThat(spaces).containsKeys("secure");
        CPSNamespace ns = spaces.get("secure");
        assertThat(ns.getName()).isEqualTo("secure");
        assertThat(ns.getVisibility()).isEqualTo(Visibility.SECURE);
    }


    @Test
    public void TestGetNamespacesDrawsFromCPSService() throws Exception {
        IConfigurationPropertyStoreService cps = new MockConfigurationPropertyStoreService();

        cps.setProperty("myNamespace.myProperties.a","myValue");
        IFramework mockFramework = new MockFramework(cps);
        CPSFacade facade = new CPSFacade(mockFramework);

        Map<String,CPSNamespace> spaces = facade.getNamespaces();

        assertThat(spaces).containsKeys("myNamespace");
        CPSNamespace ns = spaces.get("myNamespace");
        assertThat(ns.getName()).isEqualTo("myNamespace");
        assertThat(ns.getVisibility()).isEqualTo(Visibility.NORMAL);
    }

    @Test
    public void TestGetNamespacesDrawsFromCPSServiceSecureVisibilityNotOverwritten() throws Exception {
        IConfigurationPropertyStoreService cps = new MockConfigurationPropertyStoreService();

        cps.setProperty("secure.myProperties.a","myValue");

        IFramework mockFramework = new MockFramework(cps);
        CPSFacade facade = new CPSFacade(mockFramework);
        Map<String,CPSNamespace> spaces = facade.getNamespaces();

        assertThat(spaces).containsKeys("secure");
        CPSNamespace ns = spaces.get("secure");
        assertThat(ns.getName()).isEqualTo("secure");
        assertThat(ns.getVisibility()).isEqualTo(Visibility.SECURE);
    }

    private void checkGetOfNamespace(String name, boolean isExpectedToBeSecure, boolean isExpectedToBeHidden )
            throws ConfigurationPropertyStoreException{
        IConfigurationPropertyStoreService cps = new MockConfigurationPropertyStoreService();
        IFramework mockFramework = new MockFramework(cps);
        CPSFacade facade = new CPSFacade(mockFramework);
        CPSNamespace ns = facade.getNamespace(name);
        assertThat(ns.isSecure()).isEqualTo(isExpectedToBeSecure);
        assertThat(ns.isHidden()).isEqualTo(isExpectedToBeHidden);
    }

    @Test
    public void TestGetNamespaceFrameworkNamespaceIsNormal() throws ConfigurationPropertyStoreException {
        checkGetOfNamespace("framework",false,false);
    }

    @Test
    public void TestGetNamespaceSecureNamespaceIsSecure() throws ConfigurationPropertyStoreException {
        checkGetOfNamespace("secure",true,false);
    }

    @Test
    public void TestGetNamespaceDssNamespaceIsHidden() throws ConfigurationPropertyStoreException {
        checkGetOfNamespace("dss",false,true);
    }

    @Test
    public void TestGetNamespaceDexNamespaceIsHidden() throws ConfigurationPropertyStoreException {
        checkGetOfNamespace("dex",false,true);
    }
}
