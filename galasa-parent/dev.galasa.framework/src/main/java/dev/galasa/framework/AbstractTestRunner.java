/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.events.TestHeartbeatStoppedEvent;
import dev.galasa.framework.spi.events.TestRunLifecycleStatusChangedEvent;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.DssUtils;

public class AbstractTestRunner {
    
    private Log logger = LogFactory.getLog(AbstractTestRunner.class);


    protected FrameworkInitialisation frameworkInitialisation = null;

    protected IConfigurationPropertyStoreService cps;
    protected IDynamicStatusStoreService         dss;
    protected IResultArchiveStore                ras;
    protected IRun                               run;

    protected IFramework                         framework;

    protected TestRunHeartbeat heartbeat = null ;

    protected TestStructure testStructure = new TestStructure();
    
    protected boolean isPublishEventsEnabled;

    protected boolean isRunOK = true;
    protected boolean resourcesAvailable = true;


    protected void init(Properties bootstrapProperties, Properties overrideProperties) throws TestRunException {
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties, true);
            cps = frameworkInitialisation.getFramework().getConfigurationPropertyService("framework");
            dss = frameworkInitialisation.getFramework().getDynamicStatusStoreService("framework");
            run = frameworkInitialisation.getFramework().getTestRun();
            ras = frameworkInitialisation.getFramework().getResultArchiveStore();
        } catch (Exception e) {
            throw new TestRunException("Unable to initialise the Framework Services", e);
        }

        this.framework = frameworkInitialisation.getFramework();

        IRun run = this.framework.getTestRun();
        if (run == null) {
            throw new TestRunException("Unable to locate run properties");
        }

        this.isPublishEventsEnabled = isProduceEventsFeatureFlagTrue();
    }

    protected void writeTestStructure() {
        try {
            this.ras.updateTestStructure(testStructure);
        } catch (ResultArchiveStoreException e) {
            logger.warn("Unable to write the test structure to the RAS", e);
        }

    }

    // method to replace repeating "run." + run.getName() + "."... where ... is the key suffix to be passed
    protected String getDSSKeyString(String keySuffix){
        return "run." + run.getName() + "." + keySuffix;
    }

    protected boolean isProduceEventsFeatureFlagTrue() throws TestRunException {
        boolean produceEvents = false;
        try {
            String produceEventsProp = this.cps.getProperty("produce", "events");
            if (produceEventsProp != null) {
                logger.debug("CPS property framework.produce.events was found and is set to: " + produceEventsProp);
                produceEvents = Boolean.parseBoolean(produceEventsProp);
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new TestRunException("Problem reading the CPS property to check if framework event production has been activated.");
        }
        return produceEvents;
    }

    protected void stopHeartbeat() {
        if (this.heartbeat == null) {
            return;
        }

        heartbeat.shutdown();
        try {
            heartbeat.join(2000);
        } catch (Exception e) {
        }

        try {
            dss.delete(getDSSKeyString("heartbeat"));
        } catch (DynamicStatusStoreException e) {
            logger.error("Unable to delete heartbeat", e);
        }

        try {
            produceTestHeartbeatStoppedEvent();
        } catch (TestRunException e) {
            logger.error("Unable to produce a test heartbeat stopped event to the Events Service", e);
        }
    }

    private void produceTestHeartbeatStoppedEvent() throws TestRunException {
        if (this.isPublishEventsEnabled) {
            logger.debug("Producing a test heartbeat stopped event.");

            String message = String.format("Galasa test run %s's heartbeat has been stopped.", framework.getTestRunName());
            TestHeartbeatStoppedEvent testHeartbeatStoppedEvent = new TestHeartbeatStoppedEvent(this.cps, Instant.now().toString(), message);
            String topic = testHeartbeatStoppedEvent.getTopic();

            if (topic != null) {
                try {
                    framework.getEventsService().produceEvent(topic, testHeartbeatStoppedEvent);
                } catch (EventsException e) {
                    throw new TestRunException("Failed to publish a test heartbeat stopped event to the Events Service", e);
                }
            }
        }
    }

    protected void deleteRunProperties(@NotNull IFramework framework) {

        IRun run = framework.getTestRun();

        if (!run.isLocal()) { // *** Not interested in non-local runs
            return;
        }

        try {
            framework.getFrameworkRuns().delete(run.getName());
        } catch (FrameworkException e) {
            logger.error("Failed to delete run properties");
        }
    }


    protected void updateResult() throws TestRunException {
        try {
            if (this.testStructure.getResult() == null) {
                this.testStructure.setResult("UNKNOWN");
            }
            this.dss.put(getDSSKeyString("result"), this.testStructure.getResult());
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update result", e);
        }
    }

    protected void saveCPSOverridesAsArtifact(Properties overrideProperties) {
        savePropertiesAsArtifactFile(overrideProperties, "cps_overrides.properties");
    }

    protected void recordCPSProperties(IFramework framework) {
        Properties record = framework.getRecordProperties();
        savePropertiesAsArtifactFile(record, "cps_record.properties");
    }

    private void savePropertiesAsArtifactFile(Properties props, String filename) {
        try {
            ArrayList<String> propertyNames = new ArrayList<>();
            propertyNames.addAll(props.stringPropertyNames());
            Collections.sort(propertyNames);

            StringBuilder sb = new StringBuilder();
            String currentNamespace = null;
            for (String propertyName : propertyNames) {
                // Ignore empty property names. Shouldn't happen anyway, but just in case.
                propertyName = propertyName.trim();
                if (propertyName.isEmpty()) {
                    continue;
                }

                // Separate the groups of properties with a new line between namespaces.
                String[] parts = propertyName.split("\\.");
                if (!parts[0].equals(currentNamespace)) {
                    if (currentNamespace != null) {
                        sb.append("\n");
                    }
                    currentNamespace = parts[0];
                }

                sb.append(propertyName);
                sb.append("=");
                sb.append(props.getProperty(propertyName));
                sb.append("\n");
            }
            IResultArchiveStore ras = this.framework.getResultArchiveStore();
            Path rasRoot = ras.getStoredArtifactsRoot();
            Path rasProperties = rasRoot.resolve("framework").resolve(filename);
            Files.createFile(rasProperties, ResultArchiveStoreContentType.TEXT);
            Files.write(rasProperties, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Failed to save the properties to file "+filename+" - ignoring.", e);
        }
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    public IConfigurationPropertyStoreService getCPS() {
        return this.cps;
    }

    protected void updateStatus(TestRunLifecycleStatus status, String timestampDSSPropSuffix) throws TestRunException {
        Instant now = Instant.now();

        this.testStructure.setStatus(status.toString());
        if (status == TestRunLifecycleStatus.FINISHED) {
            updateResult();
            this.testStructure.setEndTime(now);
        }

        writeTestStructure();

        try {
            this.dss.put(getDSSKeyString("status"), status.toString());
            if (timestampDSSPropSuffix != null) {
                this.dss.put(getDSSKeyString(timestampDSSPropSuffix), now.toString());
            }
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update status", e);
        }

        try {
            produceTestRunLifecycleStatusChangedEvent(status);
        } catch (TestRunException e) {
            logger.error("Unable to produce a test run lifecycle status changed event to the Events Service", e);
        }
    }

    private void produceTestRunLifecycleStatusChangedEvent(TestRunLifecycleStatus status) throws TestRunException {
        if (this.isPublishEventsEnabled) {
            logger.debug("Producing a test run lifecycle status change event.");

            String message = String.format("Galasa test run %s is now in status: %s.", framework.getTestRunName(), status.toString());
            TestRunLifecycleStatusChangedEvent testRunLifecycleStatusChangedEvent = new TestRunLifecycleStatusChangedEvent(this.cps, Instant.now().toString(), message);
            String topic = testRunLifecycleStatusChangedEvent.getTopic();

            if (topic != null) {
                try {
                    framework.getEventsService().produceEvent(topic, testRunLifecycleStatusChangedEvent);
                } catch (EventsException e) {
                    throw new TestRunException("Failed to publish a test run lifecycle status changed event to the Events Service", e);
                }
            }
        }
    }

    protected void loadOverrideProperties(Properties overrideProperties) throws TestRunException {
        //*** Load the overrides if present
        try {
            String prefix = getDSSKeyString("override.");
            Map<String, String> runOverrides = dss.getPrefix(prefix);
            for(Entry<String, String> entry : runOverrides.entrySet()) {
                String key = entry.getKey().substring(prefix.length());
                String value = entry.getValue();
                overrideProperties.put(key, value);
            }
        } catch(Exception e) {
            throw new TestRunException("Problem loading overrides from the run properties", e);
        }
    }

    protected void setUnknownTestState(IRun run) {
        this.testStructure.setRunName(run.getName());
        this.testStructure.setQueued(run.getQueued());
        this.testStructure.setStartTime(Instant.now());
        this.testStructure.setRequestor(AbstractManager.defaultString(run.getRequestor(), "unknown"));
        writeTestStructure();
    }

    protected void allocateRasRunId() throws TestRunException {
        String rasRunId = this.ras.calculateRasRunId();
        try {
            this.dss.put(getDSSKeyString("rasrunid"), rasRunId);
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update rasrunid", e);
        }
    }

    protected void markWaiting(@NotNull IFramework framework) throws TestRunException {
        int initialDelay = 600;
        int randomDelay = 180;

        DssUtils.incrementMetric(dss, "metrics.runs.made.to.wait");

        try {
            String sInitialDelay = AbstractManager.nulled(this.cps.getProperty("waiting.initial", "delay"));
            String sRandomDelay = AbstractManager.nulled(this.cps.getProperty("waiting.random", "delay"));

            if (sInitialDelay != null) {
                initialDelay = Integer.parseInt(sInitialDelay);
            }
            if (sRandomDelay != null) {
                randomDelay = Integer.parseInt(sRandomDelay);
            }
        } catch (Exception e) {
            logger.error("Problem reading delay properties", e);
        }

        int totalDelay = initialDelay + framework.getRandom().nextInt(randomDelay);
        logger.info("Placing this run on waiting for " + totalDelay + " seconds");

        Instant until = Instant.now();
        until = until.plus(totalDelay, ChronoUnit.SECONDS);

        HashMap<String, String> properties = new HashMap<>();
        properties.put(getDSSKeyString("status"), "waiting");
        properties.put(getDSSKeyString("wait.until"), until.toString());
        try {
            this.dss.put(properties);
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Unable to place run in waiting state", e);
        }
    }

}
