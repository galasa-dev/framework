/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

public class GalasaNamespace {

   
    private String name;
    private String propertiesUrl;
    private String type;

    public GalasaNamespace(String namespace, String namespaceType){
        this.name = namespace;
        this.type = namespaceType;
    }

    public GalasaNamespace(String namespace, String namespaceType, String propertiesUrl){
        this(namespace, namespaceType);
        this.propertiesUrl = propertiesUrl+this.name+"/properties";
    }


    public String getName(){
        return this.name;
    }

    public String getPropertiesUrl(){
        return this.propertiesUrl;
    }

    public String getNamespaceType(){
        return this.type;
    }

}
