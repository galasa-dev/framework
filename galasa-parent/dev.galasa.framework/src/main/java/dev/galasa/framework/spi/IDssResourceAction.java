/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.framework.spi;

/**
 * IDssStatusAction is the basis for all updates to the Dynamic Status Store properties 
 * framework status area
 * 
 * @author Michael Baylis
 *
 */
public interface IDssResourceAction {
    
    IDssAction applyPrefix(String prefix);
    
}
