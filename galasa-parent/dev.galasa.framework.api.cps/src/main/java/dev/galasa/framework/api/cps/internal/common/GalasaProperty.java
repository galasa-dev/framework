/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class GalasaProperty {
    static final Gson gson = GalasaGsonBuilder.build();
    
    public String apiVersion = "v1alpha1";
    public final String kind = "GalasaProperty"; 
    public GalasaPropertyMetadata metadata ;
    public GalasaPropertyData data;
    
    public class GalasaPropertyMetadata {
        public String namespace;
        public String name;
        
        public GalasaPropertyMetadata (String namespace, String name){
            this.namespace = namespace;
            this.name = name;
        }
    }

    public class GalasaPropertyData {
        public String value;
        
        public GalasaPropertyData (String propertyValue){
            this.value = propertyValue;
        }
    }

    public GalasaProperty (String completeCPSname, String propertyValue){
        String[] name = completeCPSname.split("[.]", 2);
        this.metadata = new GalasaPropertyMetadata(name[0],name[1]);
        this.data = new GalasaPropertyData(propertyValue);
    }

    public GalasaProperty (Map.Entry<String, String> propertyEntry){
        this(propertyEntry.getKey(),propertyEntry.getValue());
    }

    public GalasaProperty (String namespace, String propertyName, String propertyValue){
        this.metadata = new GalasaPropertyMetadata(namespace, propertyName);
        this.data = new GalasaPropertyData(propertyValue);
    }

    public GalasaProperty (String namespace, String propertyName, String propertyValue, String apiVersion){
        this(namespace, propertyName, propertyValue);
        this.apiVersion = apiVersion;
    }

    public JsonObject toJSON() {
        String jsonstring = gson.toJson(this);
        return gson.fromJson(jsonstring, JsonObject.class);
    }

    public String getApiVersion(){
        return this.apiVersion;
    }

    public boolean isPropertyNameValid(){
        return this.metadata.name != null && !this.metadata.name.isBlank();
    }

    public boolean isPropertyNameSpaceValid(){
        return this.metadata.namespace != null && !this.metadata.namespace.isBlank();
    }

    public boolean isPropertyValueValid(){
        return this.data.value != null && !this.data.value.isBlank();
    }

    public boolean isPropertyApiVersionValid(){
        return this.apiVersion != null && !this.apiVersion.isBlank();
    }

    public boolean isPropertyValid() {
        return isPropertyApiVersionValid() && isPropertyNameSpaceValid() && isPropertyNameValid() && isPropertyValueValid();
    }
}
