/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class GalasaNamespace {

    private String name;
    private String propertiesUrl;
    private Visibility visibility;
    private IConfigurationPropertyStoreService propertyStore;

    protected GalasaNamespace(@NotNull String name, @NotNull Visibility visibility, @NotNull IFramework framework)
            throws ConfigurationPropertyStoreException {
        this.name = name;
        this.visibility = (visibility);
        this.propertiesUrl = "/"+this.name+"/properties";
        this.propertyStore = framework.getConfigurationPropertyService(name);
    }

    public String getName() {
        return this.name;
    }

    public String getPropertiesUrl() {
        return this.propertiesUrl;
    }

    public Visibility getVisibility() {     
        return this.visibility;
    }

    public boolean isSecure() {
        return this.visibility == Visibility.SECURE;
    }

    public boolean isHidden() {
        return this.visibility == Visibility.HIDDEN;
    }

    public Map<GalasaPropertyName,GalasaProperty> getProperties(){
        Map<GalasaPropertyName,GalasaProperty> properties = new HashMap<GalasaPropertyName,GalasaProperty>();
        if (visibility != Visibility.HIDDEN){
            Map<String,String> cpsProperties = propertyStore.getAllProperties();
            for (Map.Entry<String,String> property : cpsProperties.entrySet()){

                String propNameWithNamespace = property.getKey();
                GalasaPropertyName propName = new GalasaPropertyName(propNameWithNamespace);

                GalasaProperty prop = new GalasaProperty(this.propertyStore, this, propName, property.getValue());
                   
                properties.put(propName, prop);
                
            }
        }
        return Collections.unmodifiableMap(properties);
    }

    public GalasaProperty getProperty(String propertyName) throws ConfigurationPropertyStoreException {
        GalasaProperty prop = null;
        if (visibility != Visibility.HIDDEN) {
            GalasaPropertyName propName = new GalasaPropertyName(this.name, propertyName);
            prop = new GalasaProperty(this.propertyStore, this, propName);
            prop.LoadValueFromStore();
            if (!prop.existsInStore()) {
                prop = null;
            }
        }
        return prop;
    }

}
