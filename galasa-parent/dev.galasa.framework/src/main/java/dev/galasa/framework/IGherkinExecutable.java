/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework;

import dev.galasa.framework.spi.IGherkinManager;

public interface IGherkinExecutable {

    IGherkinManager getRegisteredManager();

    void registerManager(IGherkinManager manager) throws TestRunException;

    String getText();
    
}