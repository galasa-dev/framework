/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
public class GalasaProperty {
    static final Gson gson = GalasaGsonBuilder.build();

    public String apiVersion = "galasa-dev/v1alpha1";
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


    public GalasaProperty (String completeCPSname, String propertyValue) {
        String[] nameParts = completeCPSname.split("[.]", 2);
        String namespaceName = nameParts[0];
        String propertyName = nameParts[1];
        this.metadata = new GalasaPropertyMetadata(namespaceName,propertyName);
        this.data = new GalasaPropertyData(propertyValue);
    }

    public GalasaProperty (Map.Entry<String, String> propertyEntry) {
        this(propertyEntry.getKey(),propertyEntry.getValue());
    }

    public GalasaProperty (String namespace, String propertyName, String propertyValue) {
        this.metadata = new GalasaPropertyMetadata(namespace, propertyName);
        this.data = new GalasaPropertyData(propertyValue);
    }

    public GalasaProperty (String namespace, String propertyName, String propertyValue, String apiVersion) {
        this(namespace, propertyName, propertyValue);
        this.apiVersion = apiVersion;
    }

    public JsonObject toJSON() {
        String jsonstring = gson.toJson(this);
        return gson.fromJson(jsonstring, JsonObject.class);
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

    public boolean isPropertyNameValid() {
        return this.metadata.name != null && !this.metadata.name.isBlank();
    }

    public boolean isPropertyNameSpaceValid() {
        return this.metadata.namespace != null && !this.metadata.namespace.isBlank();
    }

    public boolean isPropertyValueValid() {
        return this.data.value != null && !this.data.value.isBlank();
    }

    public boolean isPropertyApiVersionValid() {
        return this.apiVersion != null && !this.apiVersion.isBlank();
    }

    public boolean isPropertyValid() throws InternalServletException {
        ServletError error = null;
        if (!this.isPropertyNameValid()){
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"name",this.metadata.name);
        }
        if (!this.isPropertyNameSpaceValid()){
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"namespace",this.metadata.namespace);
        }
        if (!this.isPropertyValueValid()){
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"value",this.data.value);
        }
        if (!this.isPropertyApiVersionValid()){
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"apiVersion",this.apiVersion);
        }
        if (error != null){
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return true;
    }
}
