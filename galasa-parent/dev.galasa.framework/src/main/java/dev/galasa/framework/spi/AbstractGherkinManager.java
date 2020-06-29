/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.gherkin.GherkinMethod;
import dev.galasa.framework.spi.gherkin.GherkinStatement;
import dev.galasa.framework.spi.gherkin.GherkinTest;

public abstract class AbstractGherkinManager extends AbstractManager implements IGherkinManager {

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GherkinTest gherkinTest) throws ManagerException {
        return;
    };

    @Override
    public String anyReasonGherkinTestMethodShouldBeIgnored(@NotNull GherkinMethod method) throws ManagerException {
        return null;
    };

    @Override
    public void startOfGherkinTestMethod(@NotNull GherkinMethod method) throws ManagerException {
        return;
    }

    @Override
    public String endOfGherkinTestMethod(@NotNull GherkinMethod method, @NotNull String currentResult, Throwable currentException) throws ManagerException {
        return null;
    }

    @Override
    public void executeStatement(@NotNull GherkinStatement statement) throws ManagerException {
        return;
    }
}
