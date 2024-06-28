/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.events;

public class TestRunLifecycleStatusChangedEvent extends Event {

    private final String TOPIC = "Tests.StatusChangedEvent";

    public TestRunLifecycleStatusChangedEvent(String timestamp, String message) {
        super(timestamp, message);
    }

    public String getTopic() {
        return this.TOPIC;
    }
    
}
