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
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class CPSProperty {
    static final Gson gson = GalasaGsonBuilder.build();

    private static final String REDACTED_PROPERTY_VALUE = "********";

    private CPSNamespace namespace;
    private GalasaPropertyName name;
    private IConfigurationPropertyStoreService store;
    private String value;

      public CPSProperty(IConfigurationPropertyStoreService store, CPSNamespace namespace, GalasaPropertyName propertyName) {
        this.namespace = namespace ;
        this.store = store ;
        this.name = propertyName;
    }

    public CPSProperty(IConfigurationPropertyStoreService store, CPSNamespace namespace, GalasaPropertyName propertyName, String value) {
        this(store, namespace,propertyName);
        this.value = value;
    }

    public CPSProperty (String completeCPSname, String propertyValue) {
        String[] nameParts = completeCPSname.split("[.]", 2);
        String namespaceName = nameParts[0];
        String propertyName = nameParts[1];
        this.name = new GalasaPropertyName(namespaceName, propertyName);
        this.value = propertyValue;
    }

    public CPSProperty (Map.Entry<String, String> propertyEntry) {
        this(propertyEntry.getKey(),propertyEntry.getValue());
    }

    public CPSProperty (String namespace, String propertyName, String propertyValue) {
        this.name = new GalasaPropertyName(namespace, propertyName);
        this.value = propertyValue;
    }

    public JsonObject toJSON() {
        String jsonstring = gson.toJson(this);
        return gson.fromJson(jsonstring, JsonObject.class);
    }

    public String getNamespace() {
        return this.name.getNamespaceName();
    }

    public String getName() {
        return this.name.getSimpleName();
    }

    public String getValue() {
        return this.value;
    }

    public String getOutputValue() {
        String outputValue  = this.value;
        if(namespace.getVisibility() == Visibility.SECURE){
            outputValue = REDACTED_PROPERTY_VALUE;
        }
        return outputValue;
    }

    public void LoadValueFromStore() throws ConfigurationPropertyStoreException {
        // load the value from the property store into this property object.
        // Will be null if the property isn't in the store yet.
        this.value = store.getProperty(name.getLongestPrefix(), name.getShortestSuffix());

        if ( this.value != null ) {
            if ( isSecure() ){
                this.value = REDACTED_PROPERTY_VALUE;
            }
        }
    }

    public boolean existsInStore() {
        return this.value != null;
    }

    public boolean isSecure() {
        return this.namespace.isSecure();
    }

    public boolean isPropertyNameValid() {
        return this.name.simpleName != null && !this.name.simpleName.isBlank();
    }

    public boolean isPropertyNameSpaceValid() {
        return this.name.namespaceName != null && !this.name.namespaceName.isBlank();
    }

    public boolean isPropertyValueValid() {
        return this.value != null && !this.value.isBlank();
    }

    public boolean isPropertyValid() throws InternalServletException {
        ServletError error = null;
        if (!this.isPropertyNameValid()){
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"name",this.name.simpleName);
        }
        if (!this.isPropertyNameSpaceValid()){
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"namespace",this.name.namespaceName);
        }
        if (!this.isPropertyValueValid()){
            error = new ServletError(GAL5024_INVALID_GALASAPROPERTY,"value",this.value);
        }
        if (error != null){
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return true;
    }
}
