/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi.language.gherkin;

import dev.galasa.framework.IGherkinExecutable;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.IGherkinManager;

public class GherkinStatement implements IGherkinExecutable {

    private String statement;
    private IGherkinManager registeredManager;
    
    private GherkinStatement(String statement) {
        this.statement = statement;
    }

    public static IGherkinExecutable get(String statement) {
        IGherkinExecutable executable = new GherkinStatement(statement);
        return executable;
    }

    public void registerManager(IGherkinManager manager) throws TestRunException {
        if(this.registeredManager != null) {
            throw new TestRunException("Manager already registered for statement: " + statement);
        }
        this.registeredManager = manager;
    }

    public IGherkinManager getRegisteredManager() {
        return this.registeredManager;
    }

    public String getText() {
        return this.statement;
    }
}