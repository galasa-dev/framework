/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.mocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher;

public class MockDSSStore implements IDynamicStatusStore {

    private Map<String,String> valueMap ;
    private Log logger = LogFactory.getLog(MockDSSStore.class.getName());
    public MockDSSStore(Map<String,String> valueMap) {
        this.valueMap = valueMap;
    }

    @Override
    public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'put'");
    }

    @Override
    public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'put'");
    }

    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
            throws DynamicStatusStoreException {
        logger.debug("DSS putswap of property "+key+" oldValue:"+oldValue+" newValue:"+newValue);
        valueMap.put(key,newValue);
        return true;
    }

    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
            @NotNull Map<String, String> others) throws DynamicStatusStoreException {
        logger.debug("DSS putswap of property "+key+" oldValue:"+oldValue+" newValue:"+newValue);
        valueMap.put(key,newValue);
        return true;
    }

    @Override
    public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
        String value = valueMap.get(key);
        logger.debug("DSS get of property "+key+" returning "+value);
        return value;
    }

    @Override
    public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        Map<String, String> results = new HashMap<String,String>();
        for (String key : valueMap.keySet()){
            if (key.startsWith(keyPrefix+".")){
                results.put(key,valueMap.get(key));
            }
        }
        logger.debug("DSS getPrefix of property "+keyPrefix+" returning "+results.toString());
        return results;
    }

    @Override
    public void delete(@NotNull String key) throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deletePrefix'");
    }

    @Override
    public void performActions(IDssAction... actions)
            throws DynamicStatusStoreException, DynamicStatusStoreMatchException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'performActions'");
    }

    @Override
    public UUID watch(IDynamicStatusStoreWatcher watcher, String key) throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'watch'");
    }

    @Override
    public UUID watchPrefix(IDynamicStatusStoreWatcher watcher, String keyPrefix) throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'watchPrefix'");
    }

    @Override
    public void unwatch(UUID watchId) throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unwatch'");
    }

    @Override
    public void shutdown() throws DynamicStatusStoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }
    
}
