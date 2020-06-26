/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.gherkin.GherkinTest;

public abstract class AbstractGherkinManager extends AbstractManager implements IGherkinManager {

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GherkinTest gherkinTest) throws ManagerException {
        return;
    };

    @Override
    public void gherkinProvisionGenerate() throws ManagerException {
        return;
    };
}
