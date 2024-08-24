package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.internal.events.IEventsPublisher;

public class MockEventsPublisher implements IEventsPublisher {

    public static class PublishedEvent {
        String testRunName ;
        String eventType ;
        TestRunLifecycleStatus testRunLifecycleStatus ;

        public PublishedEvent(String testRunName, String eventType) {
            this(testRunName, eventType, null);
        }
        public PublishedEvent(String testRunName, String eventType, TestRunLifecycleStatus status) {
            this.testRunName = testRunName ;
            this.eventType = eventType;
            this.testRunLifecycleStatus = status ;
        }
    }

    private boolean isEnabled = false;

    private List<PublishedEvent> history = new ArrayList<>();


    public List<PublishedEvent> getHistory() {
        return this.history;
    }

    @Override
    public void setEnabled(boolean isProduceEventsEnabled) {
        isEnabled = isProduceEventsEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void publishTestHeartbeatStoppedEvent(String testRunName) throws TestRunException {
        PublishedEvent event = new PublishedEvent(testRunName,"TestHeartbeatStoppedEvent");
        history.add(event);
    }

    @Override
    public void publishTestRunLifecycleStatusChangedEvent(String testRunName, TestRunLifecycleStatus status)
            throws TestRunException {
        PublishedEvent event = new PublishedEvent(testRunName,"TestRunLifecycleStatusChangedEvent", status);
        history.add(event);
    }
    
}
