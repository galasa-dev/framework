/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.events;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunLifecycleStatus;

/**
 * Something which publishes information about important events in the system.
 */
public interface IEventsPublisher {
    void setEnabled(boolean isProduceEventsEnabled);

    boolean isEnabled();

    void publishTestHeartbeatStoppedEvent(String testRunName) throws TestRunException ;

    void publishTestRunLifecycleStatusChangedEvent(String testRunName, TestRunLifecycleStatus status) throws TestRunException ;
}
