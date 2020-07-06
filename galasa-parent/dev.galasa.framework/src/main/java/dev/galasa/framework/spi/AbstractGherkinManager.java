/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;

public abstract class AbstractGherkinManager extends AbstractManager implements IGherkinManager {

    @Override
    public void executeGherkin(@NotNull IGherkinExecutable executable, Map<String, Object> testVariables) throws ManagerException {
        return;
    }
}
