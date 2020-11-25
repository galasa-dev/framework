/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This DSS action request that property is updated in the DSS whether it exists or not. 
 * If you want the transaction to fail if the property already exists, use DssAdd
 * 
 * @author Michael Baylis
 *
 */
public class DssUpdate implements IDssAction {
    
    private final String key;
    private final String value;

    public DssUpdate(@NotNull String key, @NotNull String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssUpdate(prefix + this.key, value);
    }


}
