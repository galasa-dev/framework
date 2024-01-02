/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
public class GalasaProperty {
    static final Gson gson = GalasaGsonBuilder.build();

    public static final String DEFAULTAPIVERSION = "galasa-dev/v1alpha1";
    private String apiVersion = DEFAULTAPIVERSION;
    private final String kind = "GalasaProperty";
    private GalasaPropertyMetadata metadata ;
    private GalasaPropertyData data;

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

    public GalasaProperty (String namespace, String propertyName, String propertyValue) {
        this.metadata = new GalasaPropertyMetadata(namespace, propertyName);
        this.data = new GalasaPropertyData(propertyValue);
    }

    public GalasaProperty (String namespace, String propertyName, String propertyValue, String apiVersion) {
        this(namespace, propertyName, propertyValue);
        this.apiVersion = apiVersion;
    }
    public GalasaProperty (CPSProperty property) {
        this(property.getNamespace(), property.getName(), property.getOutputValue());
    }

    public String getKind() {
        return this.kind;
    }

    public String getApiVersion() {
        return this.apiVersion;
    }

    public String getNamespace() {
        return this.metadata.namespace;
    }

    public String getName() {
        return this.metadata.name;
    }

    public String getValue() {
        return this.data.value;
    }

    public static GalasaProperty getPropertyFromRequestBody( String jsonString) throws IOException, InternalServletException{
         GalasaProperty property;
        try {
            property = gson.fromJson(jsonString, GalasaProperty.class);
        }catch (Exception e){
            ServletError error = new ServletError(GAL5023_UNABLE_TO_CAST_TO_GALASAPROPERTY, jsonString);  
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return property;
    }

}
