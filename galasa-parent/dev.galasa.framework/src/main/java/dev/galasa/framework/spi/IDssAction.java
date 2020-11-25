/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.framework.spi;

/**
 * IDssAction is the basis for all updates to the Dynamic Status Store properties
 * 
 * @author Michael Baylis
 *
 */
public interface IDssAction {
    
    IDssAction applyPrefix(String prefix);
    
}
