/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.mocks;

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

    public MockIConfigurationPropertyStoreService(@NotNull String namespace) {
        this.namespaceInput = namespace;
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
        
        throw new UnsupportedOperationException("Unimplemented method 'setProperty'");
    }

    @Override
    public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {
        
        throw new UnsupportedOperationException("Unimplemented method 'deleteProperty'");
    }

    @Override
    public Map<String, String> getAllProperties() {
        Map<String, String> properties = new HashMap<String,String>();
        if (namespaceInput != "multi"){
            properties.put("property1", "value1");
            properties.put("property2", "value2");
            properties.put("property3", "value3");
            properties.put("property4", "value4");
            properties.put("property5", "value5");
        }else{
            properties.put("test.property", "value1");
            properties.put(".charity1", "value2");
            properties.put(".lecture101", "value101");
            properties.put(".hospitality", "value3");
            properties.put("test.aunty5", "value4");
            properties.put("test.empty", "value5");
        }

        return properties;
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
        }
        return cpsList;
    }
    ;
}
