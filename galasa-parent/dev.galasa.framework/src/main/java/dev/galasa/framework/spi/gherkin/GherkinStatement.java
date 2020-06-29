package dev.galasa.framework.spi.gherkin;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.IGherkinManager;
import dev.galasa.framework.spi.IManager;

public class GherkinStatement {

    private String statement;
    private IGherkinManager registeredManager;
    
    public GherkinStatement(String statement) {
        this.statement = statement;
    }

    public void registerManager(IGherkinManager manager) throws TestRunException {
        if(this.registeredManager != null) {
            throw new TestRunException("Manager already registered for statement: " + statement);
        }
        this.registeredManager = manager;
    }

    protected IGherkinManager getRegisteredManager() {
        return this.registeredManager;
    }

    public String toString() {
        return this.statement;
    }
}