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

public interface IGherkinManager {

    void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GherkinTest gherkinTest) throws ManagerException;

    String anyReasonGherkinTestMethodShouldBeIgnored(@NotNull GherkinMethod method) throws ManagerException;

    void startOfGherkinTestMethod(@NotNull GherkinMethod method) throws ManagerException;

    String endOfGherkinTestMethod(@NotNull GherkinMethod method, @NotNull String currentResult, Throwable currentException) throws ManagerException;

    void executeStatement(@NotNull GherkinStatement statement) throws ManagerException;

}
