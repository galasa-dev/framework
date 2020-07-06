/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;

public interface IGherkinExecutable {

    IGherkinManager getRegisteredManager();

    void registerManager(IGherkinManager manager) throws TestRunException;

    String getValue();

    GherkinKeyword getKeyword();
    
}