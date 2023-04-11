/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.mocks;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;

public class MockCPSStore implements IConfigurationPropertyStore {

    Map<String,String> properties ;

    public MockCPSStore( Map<String,String> properties ) {
        this.properties = properties ;
    }

    @Override
    public @Null String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        return this.properties.get(key);
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrefixedProperties'");
    }

    @Override
    public void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setProperty'");
    }

    @Override
    public void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteProperty'");
    }

    @Override
    public Map<String, String> getPropertiesFromNamespace(String namespace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPropertiesFromNamespace'");
    }

    @Override
    public List<String> getNamespaces() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNamespaces'");
    }

    @Override
    public void shutdown() throws ConfigurationPropertyStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

}
