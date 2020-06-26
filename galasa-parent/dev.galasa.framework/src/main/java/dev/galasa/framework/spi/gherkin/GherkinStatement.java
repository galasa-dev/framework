package dev.galasa.framework.spi.gherkin;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.IManager;

public class GherkinStatement {

    private String statement;
    private IManager registeredManager;
    
    public GherkinStatement(String statement) {
        this.statement = statement;
    }

    public void registerManager(IManager manager) throws TestRunException {
        if(this.registeredManager != null) {
            throw new TestRunException("Manager already registered for statement: " + statement);
        }
        this.registeredManager = manager;
    }

    protected IManager getRegisteredManager() {
        return this.registeredManager;
    }
}