package io.ejat.framework.internal.cts;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.ConfidentialTextException;
import io.ejat.framework.spi.IConfidentialTextServiceRegistration;
import io.ejat.framework.spi.IFrameworkInitialisation;

public class FrameworkConfidentialTextServiceRegistration implements IConfidentialTextServiceRegistration {

     /**
     * This method intialises the service with the framework, managers can then access this service.
     * 
     * @param IFrameworkInitialisation - the framework setup.
     * @throws ConfidentialTextException
     */
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ConfidentialTextException {
        try {
            frameworkInitialisation.registerConfidentialTextService(new FrameworkConfidentialTextService());
        } catch (ConfidentialTextException e) {
            throw e;
        }
    }
}