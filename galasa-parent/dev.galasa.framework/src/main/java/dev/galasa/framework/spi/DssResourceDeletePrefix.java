/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * This action is for the framework status area of Manager.
 *
 * This DSS action request that all properties with this prefix are deleted.
 * 
 * @author Michael Baylis
 *
 */
public class DssResourceDeletePrefix extends DssDeletePrefix implements IDssResourceAction {
    
    public DssResourceDeletePrefix(@NotNull String prefix) {
        super(prefix);
    }
    
    @Override
    public IDssAction applyPrefix(String prefix) {
        return new DssDeletePrefix(prefix + getPrefix());
    }

    
}
