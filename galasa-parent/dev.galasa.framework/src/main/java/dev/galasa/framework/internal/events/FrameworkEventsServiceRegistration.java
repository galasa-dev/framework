/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.events;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.IEventsServiceRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;

@Component(service = { IEventsServiceRegistration.class })
public class FrameworkEventsServiceRegistration implements IEventsServiceRegistration {

    /**
     * This method intialises the service with the framework, managers can then
     * access this service.
     * 
     * @param frameworkInitialisation - the framework setup.
     * @throws EventsException 
     */
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws EventsException {
        try {
            frameworkInitialisation.registerEventsService(new FrameworkEventsService());
        } catch (EventsException e) {
            throw e;
        }
    }
    
}
