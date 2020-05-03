/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework;

import java.net.MalformedURLException;
import java.net.URL;
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
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SharedEnvironment;
import dev.galasa.Test;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.FrameworkResourceUnavailableException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.DssUtils;

/**
 * Run the supplied test class
 */
@Component(service = { TestRunner.class })
public class TestRunner {

    private enum RunType {
        Test,
        SharedEnvironmentBuild,
        SharedEnvironmentDiscard
    }


    private Log                                logger        = LogFactory.getLog(TestRunner.class);

    private BundleContext                      bundleContext;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private RepositoryAdmin                    repositoryAdmin;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private IMavenRepository                   mavenRepository;

    private TestRunHeartbeat                   heartbeat;

    private IConfigurationPropertyStoreService cps;
    private IDynamicStatusStoreService         dss;
    private IResultArchiveStore                ras;
    private IRun                               run;

    private TestStructure                      testStructure = new TestStructure();

    private RunType                            runType;

    /**
     * Run the supplied test class
     * 
     * @param testBundleName
     * @param testClassName
     * @return
     * @throws TestRunException
     */
    public void runTest(Properties bootstrapProperties, Properties overrideProperties) throws TestRunException {

        // *** Initialise the framework services
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties, true);
            cps = frameworkInitialisation.getFramework().getConfigurationPropertyService("framework");
            dss = frameworkInitialisation.getFramework().getDynamicStatusStoreService("framework");
            run = frameworkInitialisation.getFramework().getTestRun();
            ras = frameworkInitialisation.getFramework().getResultArchiveStore();
        } catch (Exception e) {
            throw new TestRunException("Unable to initialise the Framework Services", e);
        }

        IRun run = frameworkInitialisation.getFramework().getTestRun();
        if (run == null) {
            throw new TestRunException("Unable to locate run properties");
        }

        String testBundleName = run.getTestBundleName();
        String testClassName = run.getTestClassName();

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

        String testRepository = null;
        String testOBR = null;
        String stream = AbstractManager.nulled(run.getStream());

        this.testStructure.setRunName(run.getName());
        this.testStructure.setQueued(run.getQueued());
        this.testStructure.setStartTime(Instant.now());
        this.testStructure.setRequestor(AbstractManager.defaultString(run.getRequestor(), "unknown"));
        writeTestStructure();

        if (stream != null) {
            logger.debug("Loading test stream " + stream);
            try {
                testRepository = this.cps.getProperty("test.stream", "repo", stream);
                testOBR = this.cps.getProperty("test.stream", "obr", stream);

                //*** TODO remove this code in 0.9.0 - renames stream to test.stream to be consistent #198
                if (testRepository == null) {
                    testRepository = this.cps.getProperty("stream", "repo", stream);
                }
                if (testOBR == null) {
                    testOBR = this.cps.getProperty("stream", "obr", stream);
                }
                //*** TODO remove above code in 0.9.0
            } catch (Exception e) {
                logger.error("Unable to load stream " + stream + " settings", e);
                updateStatus("finished", "finished");
                this.ras.shutdown();
                return;
            }
        }

        String overrideRepo = AbstractManager.nulled(run.getRepository());
        if (overrideRepo != null) {
            testRepository = overrideRepo;
        }
        String overrideOBR = AbstractManager.nulled(run.getOBR());
        if (overrideOBR != null) {
            testOBR = overrideOBR;
        }

        if (testRepository != null) {
            logger.debug("Loading test maven repository " + testRepository);
            try {
                String[] repos = testRepository.split("\\,");
                for(String repo : repos) {
                    repo = repo.trim();
                    if (!repo.isEmpty()) {
                        this.mavenRepository.addRemoteRepository(new URL(repo));
                    }
                }
            } catch (MalformedURLException e) {
                logger.error("Unable to add remote maven repository " + testRepository, e);
                updateStatus("finished", "finished");
                this.ras.shutdown();
                return;
            }
        }

        if (testOBR != null) {
            logger.debug("Loading test obr repository " + testOBR);
            try {
                String[] testOBRs = testOBR.split("\\,");
                for(String obr : testOBRs) {
                    obr = obr.trim();
                    if (!obr.isEmpty()) {
                        repositoryAdmin.addRepository(obr);
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to load specified OBR " + testOBR, e);
                updateStatus("finished", "finished");
                this.ras.shutdown();
                return;
            }
        }

        try {
            BundleManagement.loadBundle(repositoryAdmin, bundleContext, testBundleName);
        } catch (Exception e) {
            logger.error("Unable to load the test bundle " + testBundleName, e);
            updateStatus("finished", "finished");
            this.ras.shutdown();
            return;
        }

        Class<?> testClass = getTestClass(testBundleName, testClassName);
        Test testAnnotation = testClass.getAnnotation(Test.class);
        SharedEnvironment sharedEnvironmentAnnotation = testClass.getAnnotation(SharedEnvironment.class);

        if (testAnnotation == null && sharedEnvironmentAnnotation == null) {
            throw new TestRunException("Class " + testBundleName + "/" + testClassName + " is neither a Test or SharedEnvironment");
        } else if (testAnnotation != null && sharedEnvironmentAnnotation != null) {
            throw new TestRunException("Class " + testBundleName + "/" + testClassName + " is both a Test and a SharedEnvironment");
        }

        if (testAnnotation != null) {
            logger.info("Run test: " + testBundleName + "/" + testClassName);
            this.runType = RunType.Test;
        } else {
            logger.info("Shared Environment class: " + testBundleName + "/" + testClassName);
        }


        if (sharedEnvironmentAnnotation != null) {
            try {
                SharedEnvironmentRunType seType = frameworkInitialisation.getFramework().getSharedEnvironmentRunType();
                if (seType != null) {
                    switch(seType) {
                        case BUILD:
                            this.runType = RunType.SharedEnvironmentBuild;
                            break;
                        case DISCARD:
                            this.runType = RunType.SharedEnvironmentDiscard;
                            break;
                        default:
                            throw new TestRunException("Unknown Shared Environment phase, '" + seType + "', needs to be BUILD or DISCARD");
                    }
                } else {
                    throw new TestRunException("Unknown Shared Environment phase, needs to be BUILD or DISCARD");
                }
            } catch(TestRunException e) {
                throw e;
            } catch(Exception e) {
                throw new TestRunException("Unable to determine the phase of the shared environment", e);
            }
        }

        if (this.runType == RunType.Test) {
            try {
                heartbeat = new TestRunHeartbeat(frameworkInitialisation.getFramework());
                heartbeat.start();
            } catch (DynamicStatusStoreException e1) {
                this.ras.shutdown();
                throw new TestRunException("Unable to initialise the heartbeat");
            }

            if (run.isLocal()) {
                DssUtils.incrementMetric(dss, "metrics.runs.local");
            } else {
                DssUtils.incrementMetric(dss, "metrics.runs.automated");
            }
        } else if (this.runType == RunType.SharedEnvironmentBuild) {
            int expireHours = sharedEnvironmentAnnotation.expireAfterHours();
            Instant expire = Instant.now().plus(expireHours, ChronoUnit.HOURS);
            try {
                this.dss.put("run." + this.run.getName() + ".shared.environment.expire", expire.toString());
            } catch (DynamicStatusStoreException e) {
                deleteRunProperties(frameworkInitialisation.getFramework());
                this.ras.shutdown();
                throw new TestRunException("Unable to set the shared environment expire time",e);
            }
        }

        updateStatus("started", "started");


        // *** Initialise the Managers ready for the test run
        TestRunManagers managers = null;
        try {
            managers = new TestRunManagers(frameworkInitialisation.getFramework(), testClass);
        } catch (FrameworkException e) {
            this.ras.shutdown();
            throw new TestRunException("Problem initialising the Managers for a test run", e);
        }

        try {
            if (managers.anyReasonTestClassShouldBeIgnored()) {
                stopHeartbeat();
                updateStatus("finished", "finished");
                this.ras.shutdown();
                return; // TODO handle ignored classes
            }
        } catch (FrameworkException e) {
            throw new TestRunException("Problem asking Managers for an ignore reason", e);
        }

        TestClassWrapper testClassWrapper = new TestClassWrapper(this, testBundleName, testClass, testStructure);

        testClassWrapper.parseTestClass();

        testClassWrapper.instantiateTestClass();

        boolean resourcesUnavailable = false;
        boolean allOk = true;

        if (this.runType == RunType.SharedEnvironmentBuild) {
            //*** Check all the active Managers to see if they support a shared environment build
            boolean invalidManager = false;
            for(IManager manager : managers.getActiveManagers()) {
                if (!manager.doYouSupportSharedEnvironments()) {
                    logger.error("Manager " + manager.getClass().getName() + " does not support Shared Environments");
                    invalidManager = true;
                }
            }

            if (invalidManager) {
                logger.error("There are Managers that do not support Shared Environment builds");
                testClassWrapper.setResult(Result.failed("Invalid Shared Environment build"));
                testStructure.setResult(testClassWrapper.getResult().getName());
                allOk = false;
            }
        }

        if (allOk) {
            try {
                updateStatus("generating", null);
                managers.provisionGenerate();
            } catch (Exception e) { 
                logger.info("Provision Generate failed", e);
                if (!(e instanceof FrameworkResourceUnavailableException)) {
                    testClassWrapper.setResult(Result.envfail(e));
                    testStructure.setResult(testClassWrapper.getResult().getName());
                }
                allOk = false;
            }
        }

        if (this.runType == RunType.Test || this.runType == RunType.SharedEnvironmentBuild) {
            if (allOk) {
                try {
                    updateStatus("building", null);
                    managers.provisionBuild();
                } catch (Exception e) {
                    managers.provisionDiscard();
                    this.ras.shutdown();
                    throw new TestRunException("Unable to provision build", e);
                }
            }

            if (allOk) {
                try {
                    updateStatus("provstart", null);
                    managers.provisionStart();
                } catch (Exception e) {
                    managers.provisionStop();
                    managers.provisionDiscard();
                    this.ras.shutdown();
                    throw new TestRunException("Unable to provision start", e);
                }
            }

            if (allOk) {
                updateStatus("running", null);
                testClassWrapper.runTestMethods(managers);
            }
        }

        if (!allOk || this.runType == RunType.Test || this.runType == RunType.SharedEnvironmentDiscard) {
            updateStatus("stopping", null);
            managers.provisionStop();
            updateStatus("discarding", null);
            managers.provisionDiscard();
            updateStatus("ending", null);
            managers.endOfTestRun();

            boolean markedWaiting = false;

            if (resourcesUnavailable && !run.isLocal()) {
                markWaiting(frameworkInitialisation.getFramework());
                logger.info("Placing queue on the waiting list");
                markedWaiting = true;
            } else {
                if (this.runType == RunType.SharedEnvironmentDiscard) {
                    this.testStructure.setResult("Discarded");
                    try {
                        this.dss.deletePrefix("run." + this.run.getName() + ".shared.environment");
                    } catch (DynamicStatusStoreException e) {
                        logger.error("Problem cleaning shared environment properties", e);
                    }
                }
                updateStatus("finished", "finished");
            }

            stopHeartbeat();

            // *** Record all the CPS properties that were accessed
            recordCPSProperties(frameworkInitialisation);

            // *** If this was a local run, then we will want to remove the run properties
            // from the DSS immediately
            // *** for automation, we will let the core manager clean up after a while
            // *** Local runs will have access to the run details via a view,
            // *** But automation runs will only exist in the RAS if we delete them, so need
            // to give
            // *** time for things like jenkins and other run requesters to obtain the
            // result and RAS id before
            // *** deleting, default is to keep the automation run properties for 5 minutes
            if (!markedWaiting) {
                deleteRunProperties(frameworkInitialisation.getFramework());
            }
        } else if (this.runType == RunType.SharedEnvironmentBuild) {
            recordCPSProperties(frameworkInitialisation);
            updateStatus("up", "built");
        }

        managers.shutdown();

        this.ras.shutdown();
        return;
    }

    private void markWaiting(@NotNull IFramework framework) throws TestRunException {
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
        properties.put("run." + run.getName() + ".status", "waiting");
        properties.put("run." + run.getName() + ".wait.until", until.toString());
        try {
            this.dss.put(properties);
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Unable to place run in waiting state", e);
        }
    }

    private void updateStatus(String status, String timestamp) throws TestRunException {

        this.testStructure.setStatus(status);
        if ("finished".equals(status)) {
            updateResult();
            this.testStructure.setEndTime(Instant.now());
        }

        writeTestStructure();

        try {
            this.dss.put("run." + run.getName() + ".status", status);
            if (timestamp != null) {
                this.dss.put("run." + run.getName() + "." + timestamp, Instant.now().toString());
            }
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update status", e);
        }
    }

    private void updateResult() throws TestRunException {
        try {
            if (this.testStructure.getResult() == null) {
                this.testStructure.setResult("UNKNOWN");
            }
            this.dss.put("run." + run.getName() + ".result", this.testStructure.getResult());
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update result", e);
        }
    }

    private void stopHeartbeat() {
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
    }

    private void writeTestStructure() {
        try {
            this.ras.updateTestStructure(testStructure);
        } catch (ResultArchiveStoreException e) {
            logger.warn("Unable to write the test structure to the RAS", e);
        }

    }

    private void deleteRunProperties(@NotNull IFramework framework) {

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

    /**
     * Get the test class from the supplied bundle
     * 
     * @param testBundleName
     * @param testClassName
     * @return
     * @throws TestRunException
     */
    private Class<?> getTestClass(String testBundleName, String testClassName) throws TestRunException {
        Class<?> testClazz = null;
        Bundle[] bundles = bundleContext.getBundles();
        boolean bundleFound = false;
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(testBundleName)) {
                bundleFound = true;
                logger.trace("Found Bundle: " + testBundleName);
                try {
                    testClazz = bundle.loadClass(testClassName);
                } catch (ClassNotFoundException e) {
                    throw new TestRunException("Unable to load test class " + testClassName, e);
                }
                logger.trace("Found test class: " + testClazz.getName());

                break;
            }
        }
        if (!bundleFound) {
            throw new TestRunException("Unable to find test bundle " + testBundleName);
        }
        return testClazz;
    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }


    private void recordCPSProperties(FrameworkInitialisation frameworkInitialisation) {
        try {
            Properties record = frameworkInitialisation.getFramework().getRecordProperties();

            ArrayList<String> propertyNames = new ArrayList<>();
            propertyNames.addAll(record.stringPropertyNames());
            Collections.sort(propertyNames);

            StringBuilder sb = new StringBuilder();
            String currentNamespace = null;
            for (String propertyName : propertyNames) {
                propertyName = propertyName.trim();
                if (propertyName.isEmpty()) {
                    continue;
                }

                String[] parts = propertyName.split("\\.");
                if (!parts[0].equals(currentNamespace)) {
                    if (currentNamespace != null) {
                        sb.append("\n");
                    }
                    currentNamespace = parts[0];
                }

                sb.append(propertyName);
                sb.append("=");
                sb.append(record.getProperty(propertyName));
                sb.append("\n");
            }
            IResultArchiveStore ras = frameworkInitialisation.getFramework().getResultArchiveStore();
            Path rasRoot = ras.getStoredArtifactsRoot();
            Path rasProperties = rasRoot.resolve("framework").resolve("cps_record.properties");
            Files.createFile(rasProperties, ResultArchiveStoreContentType.TEXT);
            Files.write(rasProperties, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Failed to save the recorded properties", e);
        }
    }

}
