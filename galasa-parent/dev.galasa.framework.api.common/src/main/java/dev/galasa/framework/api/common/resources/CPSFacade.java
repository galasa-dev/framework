/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;


import java.util.*;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

import dev.galasa.framework.spi.IFramework;

public class CPSFacade {

    private Map<String,GalasaNamespace> bakedInNamespaceMap = new HashMap<String,GalasaNamespace>();
    private IFramework framework;

    public CPSFacade(IFramework framework) throws ConfigurationPropertyStoreException  {
        this.framework = framework;
        populateBakedInNamespace(framework);
    }

    private void populateBakedInNamespace(IFramework framework) throws ConfigurationPropertyStoreException  {
        addNamespace("framework",Visibility.NORMAL,framework);
        addNamespace("secure",Visibility.SECURE,framework);
        addNamespace("dss",Visibility.HIDDEN,framework);
    }

    private void addNamespace(String name, Visibility visibility , @NotNull IFramework framework ) throws ConfigurationPropertyStoreException  {
        bakedInNamespaceMap.put(name, new GalasaNamespace(name,visibility,framework));
    }

    public Map<String,GalasaNamespace> getNamespaces() throws ConfigurationPropertyStoreException  {

        List<String> namespaceNames = framework.getConfigurationPropertyService("framework").getCPSNamespaces();
        for( String name : namespaceNames ) {
            if (! bakedInNamespaceMap.containsKey(name) ) {
                addNamespace(name,Visibility.NORMAL,framework);
            }
        }

        return Collections.unmodifiableMap(bakedInNamespaceMap);
    }

    public GalasaNamespace getNamespace(String name) throws ConfigurationPropertyStoreException  {
        GalasaNamespace namespace = bakedInNamespaceMap.get(name);
        if (namespace == null) {
            new GalasaNamespace(name, Visibility.NORMAL ,this.framework);       
        }
        return namespace;
    }
}
