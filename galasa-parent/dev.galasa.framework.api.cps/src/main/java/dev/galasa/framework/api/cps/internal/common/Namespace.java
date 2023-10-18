/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import java.util.HashSet;
import java.util.Set;

public class Namespace {

   
    private String name;
    private String propertiesUrl;
    private NamespaceType type;

    public Namespace(String namespace){
        this.name = namespace;
        if (isSecureNamespace()){
            this.type= NamespaceType.SECURE;
        }else{
            this.type=NamespaceType.NORMAL;
        }
    }

    public Namespace(String namespace, String propertiesUrl){
        this(namespace);
        this.propertiesUrl = propertiesUrl+this.name+"/properties";
    }


    private static final String REDACTED_PROPERTY_VALUE = "********";

    private static final Set<String> hiddenNamespaces = new HashSet<>();
    static {
        hiddenNamespaces.add("dss");
    }

    /**
     * Some namespaces are able to be set, but cannot be queried.
     *
     * When they are queried, the values are redacted
     */
    private static final Set<String> secureNamespaces = new HashSet<>();
    static {
        secureNamespaces.add("secure");
    }

    public boolean isHiddenNamespace(){
        return hiddenNamespaces.contains(this.name);
    }
    
    public boolean isSecureNamespace(){
        return secureNamespaces.contains(this.name);
    }
    
    public String getProtectedValue(String actualValue) {
        String protectedValue ;
        if (secureNamespaces.contains(this.name)) {
            // The namespace is protected, write-only, so should not be readable.
            protectedValue = REDACTED_PROPERTY_VALUE;
        } else {
            protectedValue = actualValue ;
        }
        return protectedValue ;
    }
    public String getName(){
        return this.name;
    }

    public String getPropertiesUrl(){
        return this.propertiesUrl;
    }

    public String getNamespaceType(){
        return this.type.toString();
    }

}
