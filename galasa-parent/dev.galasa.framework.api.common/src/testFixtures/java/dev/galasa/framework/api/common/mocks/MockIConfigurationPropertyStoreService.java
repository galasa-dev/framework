/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class MockIConfigurationPropertyStoreService implements IConfigurationPropertyStoreService{

    protected String namespaceInput;
    Map<String, String> properties = new HashMap<String,String>();

    public MockIConfigurationPropertyStoreService(@NotNull String namespace) {
        this.namespaceInput = namespace;
        if (this.namespaceInput == "multi"){
            this.properties.put("multi.test.property", "value1");
            this.properties.put("multi..charity1", "value2");
            this.properties.put("multi..lecture101", "value101");
            this.properties.put("multi..hospitality", "value3");
            this.properties.put("multi.test.aunty5", "value4");
            this.properties.put("multi.test.empty", "value5");
        }else if (this.namespaceInput == "infixes"){
            this.properties.put(namespace+".test.aproperty.stream", "value1");
            this.properties.put(namespace+".test.bproperty.stream", "value2");
            this.properties.put(namespace+".test.property.testing.local.stream", "value3");
            this.properties.put(namespace+".test.property.testing.stream", "value4");
            this.properties.put(namespace+".test.stream", "value5");
        }else{
            this.properties.put(namespace+".property1", "value1");
            this.properties.put(namespace+".property2", "value2");
            this.properties.put(namespace+".property3", "value3");
            this.properties.put(namespace+".property4", "value4");
            this.properties.put(namespace+".property5", "value5");
        }
	}

    public String getNamespaceInput(){
        return this.namespaceInput;
    }

    @Override
    public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
        
        throw new UnsupportedOperationException("Unimplemented method 'getProperty'");
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPrefixedProperties'");
    }

    @Override
    public void setProperty(@NotNull String name, @NotNull String value) throws ConfigurationPropertyStoreException {
       this.properties.put(this.namespaceInput+"."+name,value);
    }

    @Override
    public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {
       this.properties.remove(this.namespaceInput+"."+name);
    }

    @Override
    public Map<String, String> getAllProperties() {
        return this.properties;
    }

    @Override
    public String[] reportPropertyVariants(@NotNull String prefix, @NotNull String suffix, String... infixes) {
        
        throw new UnsupportedOperationException("Unimplemented method 'reportPropertyVariants'");
    }

    @Override
    public String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes) {
        
        throw new UnsupportedOperationException("Unimplemented method 'reportPropertyVariantsString'");
    }

    @Override
    public List<String> getCPSNamespaces() {
        ArrayList<String> cpsList = new ArrayList<String>();
        if (this.namespaceInput !="empty"){
            cpsList.add("nampespace1");
            cpsList.add("nampespace2");
            cpsList.add("nampespace3");
            cpsList.add("nampespace4");
            cpsList.add("nampespace5");
            cpsList.add("nampespace6");
            cpsList.add("nampespace7");
            cpsList.add("secure");
        }
        return cpsList;
    }
    ;
}
