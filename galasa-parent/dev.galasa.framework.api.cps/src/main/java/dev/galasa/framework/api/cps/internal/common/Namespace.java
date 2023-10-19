/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import dev.galasa.framework.api.cps.internal.routes.CPSRoute;

public class Namespace {

   
    private String name;
    private String propertiesUrl;
    private String type;

    public Namespace(String namespace){
        this.name = namespace;
        if (CPSRoute.isSecureNamespace(namespace)){
            this.type= NamespaceType.SECURE.toString();
        }else{
            this.type=NamespaceType.NORMAL.toString();
        }
    }

    public Namespace(String namespace, String propertiesUrl){
        this(namespace);
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
