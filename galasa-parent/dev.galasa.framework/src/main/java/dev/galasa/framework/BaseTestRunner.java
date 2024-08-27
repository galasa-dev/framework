/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

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
import dev.galasa.framework.spi.IShuttableFramework;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.events.TestHeartbeatStoppedEvent;
import dev.galasa.framework.spi.events.TestRunLifecycleStatusChangedEvent;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.DssUtils;

public class BaseTestRunner {

    private Log logger = LogFactory.getLog(BaseTestRunner.class);

    protected BundleContext bundleContext;

    protected IShuttableFramework framework;
    protected IBundleManager bundleManager;
    protected IFileSystem fileSystem;

    protected IConfigurationPropertyStoreService cps;
    protected IDynamicStatusStoreService dss;
    protected IResultArchiveStore ras;
    protected IRun run;

    protected TestStructure testStructure ;

    protected TestRunHeartbeat heartbeat;

    protected boolean isRunOK = true;
    protected boolean isResourcesAvailable = true;

    protected boolean isProduceEventsEnabled;

    protected Properties overrideProperties;

    protected void init(ITestRunnerDataProvider dataProvider) throws TestRunException {
        this.run = dataProvider.getRun() ;
        this.framework = dataProvider.getFramework();
        this.cps = dataProvider.getCPS();
        this.ras = dataProvider.getRAS();
        this.dss = dataProvider.getDSS();
        this.bundleManager = dataProvider.getBundleManager();
        this.fileSystem = dataProvider.getFileSystem();

        this.overrideProperties = dataProvider.getOverrideProperties();

        this.isProduceEventsEnabled = isProduceEventsFeatureFlagTrue();

        checkRunIsSet(this.run);

        loadOverrideProperties(this.overrideProperties, this.run, this.dss);
    }

    protected void shutdownFramework(IShuttableFramework framework) {
        try {
            framework.shutdown();
        } catch(Exception e) {
            logger.fatal("Problem shutting down the Galasa framework",e);
        }
    }

    protected void saveUsedCPSPropertiesToArtifact(
        Properties props, 
        IFileSystem fileSystem, 
        IResultArchiveStore ras
    ) {;
        String titleText = "The properties used by the test and managers.";
        String fileName = "cps_record.properties";

        savePropertiesToFile( ras, props , titleText , fileName , fileSystem );
    }

    protected void saveAllOverridesPassedToArtifact(
        Properties overrides,
        IFileSystem fileSystem,
        IResultArchiveStore ras
    ) {
        String titleText = "The properties passed as overrides to the test";
        String fileName = "overrides.properties";

        savePropertiesToFile( ras , overrides, titleText, fileName , fileSystem);
    }

    private void savePropertiesToFile( IResultArchiveStore ras, Properties props, String titleText, String fileName , IFileSystem fileSystem) {
        try {
            ArrayList<String> propertyNames = new ArrayList<>();
            propertyNames.addAll(props.stringPropertyNames());

            Collections.sort(propertyNames);

            StringBuilder sb = new StringBuilder();

            sb.append("# ");
            sb.append(titleText);
            sb.append("\n\n");

            String currentNamespace = null;
            for (String propertyName : propertyNames) {
                propertyName = propertyName.trim();
                if (propertyName.isEmpty()) {
                    continue;
                }

                // Put out a blank line between namespaces.
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
            
            Path rasRoot = ras.getStoredArtifactsRoot();
            Path rasProperties = rasRoot.resolve("framework").resolve(fileName);
            fileSystem.createFile(rasProperties, ResultArchiveStoreContentType.TEXT);
            fileSystem.write(rasProperties, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Failed to save the "+fileName+" properties. "+titleText, e);
        }
    }

    private boolean isProduceEventsFeatureFlagTrue() throws TestRunException {
        boolean produceEvents = false;
        try {
            String produceEventsProp = this.cps.getProperty("produce", "events");
            if (produceEventsProp != null) {
                logger.debug("CPS property framework.produce.events was found and is set to: " + produceEventsProp);
                produceEvents = Boolean.parseBoolean(produceEventsProp);
            }
        } catch (ConfigurationPropertyStoreException ex) {
            throw new TestRunException("Problem reading the CPS property to check if framework event production has been activated.",ex);
        }
        return produceEvents;
    }

    private void checkRunIsSet(IRun run) throws TestRunException {
        if (run == null) {
            throw new TestRunException("Unable to locate run properties");
        }
    }

    private void loadOverrideProperties(Properties overrideProperties,
                                        IRun run, 
                                        IDynamicStatusStoreService dss) throws TestRunException {
        //*** Load the overrides if present
        try {
            String prefix = "run." + run.getName() + ".override.";
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

    protected TestStructure createNewTestStructure(IRun run) {
        TestStructure testStructure = new TestStructure();

        String runName = run.getName();
        Instant queuedAt = run.getQueued();
        String requestor = AbstractManager.defaultString(run.getRequestor(), "unknown");         

        testStructure.setQueued(queuedAt);
        testStructure.setStartTime(Instant.now());
        testStructure.setRunName(runName);
        testStructure.setRequestor(requestor);
        return testStructure;
    }

    protected void writeTestStructure() {
        try {
            this.ras.updateTestStructure(testStructure);
        } catch (ResultArchiveStoreException e) {
            logger.warn("Unable to write the test structure to the RAS", e);
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

    // method to replace repeating "run." + run.getName() + "."... where ... is the key suffix to be passed
    protected String getDSSKeyString(String keySuffix){
        return "run." + run.getName() + "." + keySuffix;
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
            dss.delete("run." + run.getName() + ".heartbeat");
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
        if (this.isProduceEventsEnabled) {
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

    protected void updateResult() throws TestRunException {
        try {
            if (this.testStructure.getResult() == null) {
                this.testStructure.setResult("UNKNOWN");
            }
            this.dss.put("run." + run.getName() + ".result", this.testStructure.getResult());
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update result", e);
        }
    }

    protected IFramework getFramework() {
        return this.framework;
    }

    public IConfigurationPropertyStoreService getCPS() {
        return this.cps;
    }

    protected void updateStatus(TestRunLifecycleStatus status, String dssTimePropSuffix) throws TestRunException {
        Instant time = Instant.now();

        this.testStructure.setStatus(status.toString());
        if ("finished".equals(status.toString())) {
            updateResult();
            this.testStructure.setEndTime(Instant.now());
        }

        writeTestStructure();

        try {
            this.dss.put(getDSSKeyString("status"), status.toString());
            if (dssTimePropSuffix != null) {
                this.dss.put(getDSSKeyString(dssTimePropSuffix), time.toString());
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
        if (this.isProduceEventsEnabled) {
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

    protected void incrimentMetric(IDynamicStatusStoreService dss, IRun run) {
        if (run.isLocal()) {
            logger.debug("It's a local test");
            DssUtils.incrementMetric(dss, "metrics.runs.local");
        } else {
            logger.debug("It's an automated test");
            DssUtils.incrementMetric(dss, "metrics.runs.automated");
        }
    }


    protected void storeRasRunIdInDss(IDynamicStatusStoreService dss, String rasRunId) throws TestRunException {
        try {
            this.dss.put("run." + run.getName() + ".rasrunid", rasRunId);
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update rasrunid", e);
        }
    }


    protected TestRunHeartbeat createBeatingHeart(IFramework framework) throws TestRunException {
        TestRunHeartbeat heartbeat;
        try {
            heartbeat = new TestRunHeartbeat(framework);
            heartbeat.start();
        } catch (DynamicStatusStoreException ex) {
            throw new TestRunException("Unable to initialise the heartbeat. "+ex.getMessage(), ex);
        }
        return heartbeat;
    }
}


