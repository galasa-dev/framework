package dev.galasa.framework.internal.events;

import java.time.Instant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.events.IEvent;
import dev.galasa.framework.spi.events.TestHeartbeatStoppedEvent;
import dev.galasa.framework.spi.events.TestRunLifecycleStatusChangedEvent;

/**
 * An implementation of something which publishes events through the OSGi registered events service.
 */
public class EventsPublisher implements IEventsPublisher {

    private Log logger = LogFactory.getLog(EventsPublisher.class);
    
    private IConfigurationPropertyStoreService cps;
    private IEventsService eventsService ;
    private boolean isEnabled = false;
    
    public EventsPublisher(IEventsService eventsService, IConfigurationPropertyStoreService cps) {
        this.eventsService = eventsService ;
        this.cps = cps ;
    }

    @Override
    public void setEnabled(boolean isProduceEventsEnabled) {
        this.isEnabled = isProduceEventsEnabled;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void publishTestHeartbeatStoppedEvent(String testRunName) throws TestRunException {
        if (this.isEnabled) {
            logger.debug("Producing a test heartbeat stopped event.");

            String message = String.format("Galasa test run %s's heartbeat has been stopped.",testRunName );
            TestHeartbeatStoppedEvent event = new TestHeartbeatStoppedEvent(this.cps, Instant.now().toString(), message);
            String topic = event.getTopic();

            publishEvent(topic,event);
        }
    }

    @Override
    public void publishTestRunLifecycleStatusChangedEvent(String testRunName, TestRunLifecycleStatus status) throws TestRunException {
        if (this.isEnabled) {
            logger.debug("Producing a test run lifecycle status change event.");

            String message = String.format("Galasa test run %s is now in status: %s.", testRunName , status.toString());
            TestRunLifecycleStatusChangedEvent event = new TestRunLifecycleStatusChangedEvent(this.cps, Instant.now().toString(), message);
            String topic = event.getTopic();

            publishEvent(topic,event);
        }
    }

    private void publishEvent(String topic, IEvent event) throws TestRunException {
        if (topic != null) {
            try {
                this.eventsService.produceEvent(topic, event);
            } catch (EventsException e) {
                throw new TestRunException("Failed to publish a test run lifecycle status changed event to the Events Service", e);
            }
        }
    }
}
