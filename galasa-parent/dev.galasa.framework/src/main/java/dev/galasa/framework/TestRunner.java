/*
 * Copyright contributors to the Galasa project
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
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
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
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.DssUtils;

/**
 * Run the supplied test class
 */
@Component(service = { TestRunner.class })
public class TestRunner {

    private enum RunType {
        TEST,
        SHAREDENVIRONMENTBUILD,
        SHAREDENVIRONMENTDISCARD
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

    private boolean                            isRunOK = true;
    private boolean                            resourcesAvailable = true;

    private IFramework                          framework;

    /**
     * Run the supplied test class
     * 
     * @param bootstrapProperties
     * @param overrideProperties
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

        this.framework = frameworkInitialisation.getFramework();

        IRun run = this.framework.getTestRun();
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
        
        String rasRunId = this.ras.calculateRasRunId();
        try {
            this.dss.put("run." + run.getName() + ".rasrunid", rasRunId);
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update rasrunid", e);
        }

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
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                frameworkInitialisation.shutdownFramework();
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
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                frameworkInitialisation.shutdownFramework();
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
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                frameworkInitialisation.shutdownFramework();
                return;
            }
        }

        try {
            BundleManagement.loadBundle(repositoryAdmin, bundleContext, testBundleName);
        } catch (Exception e) {
            logger.error("Unable to load the test bundle " + testBundleName, e);
            updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
            frameworkInitialisation.shutdownFramework();
            return;
        }
        
        Class<?> testClass;
        try {
            testClass = getTestClass(testBundleName, testClassName);
        } catch(Throwable t) {
            logger.error("Problem locating test " + testBundleName + "/" + testClassName, t);
            updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
            frameworkInitialisation.shutdownFramework();
            return;
        }
        Test testAnnotation = testClass.getAnnotation(Test.class);
        SharedEnvironment sharedEnvironmentAnnotation = testClass.getAnnotation(SharedEnvironment.class);

        if (testAnnotation == null && sharedEnvironmentAnnotation == null) {
            throw new TestRunException("Class " + testBundleName + "/" + testClassName + " is not annotated with either the dev.galasa @Test or @SharedEnvironment annotations");
        } else if (testAnnotation != null && sharedEnvironmentAnnotation != null) {
            throw new TestRunException("Class " + testBundleName + "/" + testClassName + " is annotated with both the dev.galasa @Test and @SharedEnvironment annotations");
        }

        if (testAnnotation != null) {
            logger.info("Run test: " + testBundleName + "/" + testClassName);
            this.runType = RunType.TEST;
        } else {
            logger.info("Shared Environment class: " + testBundleName + "/" + testClassName);
        }


        if (sharedEnvironmentAnnotation != null) {
            try {
                SharedEnvironmentRunType seType = this.framework.getSharedEnvironmentRunType();
                if (seType != null) {
                    switch(seType) {
                        case BUILD:
                            this.runType = RunType.SHAREDENVIRONMENTBUILD;
                            break;
                        case DISCARD:
                            this.runType = RunType.SHAREDENVIRONMENTDISCARD;
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

        if (this.runType == RunType.TEST) {
            try {
                heartbeat = new TestRunHeartbeat(this.framework);
                heartbeat.start();
            } catch (DynamicStatusStoreException e1) {
                frameworkInitialisation.shutdownFramework();
                throw new TestRunException("Unable to initialise the heartbeat");
            }

            if (run.isLocal()) {
                DssUtils.incrementMetric(dss, "metrics.runs.local");
            } else {
                DssUtils.incrementMetric(dss, "metrics.runs.automated");
            }
        } else if (this.runType == RunType.SHAREDENVIRONMENTBUILD) {
            int expireHours = sharedEnvironmentAnnotation.expireAfterHours();
            Instant expire = Instant.now().plus(expireHours, ChronoUnit.HOURS);
            try {
                this.dss.put("run." + this.run.getName() + ".shared.environment.expire", expire.toString());
            } catch (DynamicStatusStoreException e) {
                deleteRunProperties(this.framework);
                frameworkInitialisation.shutdownFramework();
                throw new TestRunException("Unable to set the shared environment expire time",e);
            }
        }

        updateStatus(TestRunLifecycleStatus.STARTED, "started");

        // *** Try to load the Core Manager bundle, even if the test doesn't use it, and if not already active
        if (!BundleManagement.isBundleActive(bundleContext, "dev.galasa.core.manager")) {
            try {
                BundleManagement.loadBundle(repositoryAdmin, bundleContext, "dev.galasa.core.manager");
            } catch (FrameworkException e) {
                logger.warn("Tried to load the Core Manager bundle, but failed, test can continue without it",e);
            }
        }


        // *** Initialise the Managers ready for the test run
        TestRunManagers managers = null;
        try {
            managers = new TestRunManagers(this.framework, new GalasaTest(testClass));
        } catch (FrameworkException e) {
            frameworkInitialisation.shutdownFramework();
            throw new TestRunException("Problem initialising the Managers for a test run", e);
        }

        try {
            if (managers.anyReasonTestClassShouldBeIgnored()) {
                stopHeartbeat();
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                frameworkInitialisation.shutdownFramework();
                return; // TODO handle ignored classes
            }
        } catch (FrameworkException e) {
            throw new TestRunException("Problem asking Managers for an ignore reason", e);
        }

        TestClassWrapper testClassWrapper;
        try { 
            testClassWrapper = new TestClassWrapper(this, testBundleName, testClass, testStructure);
        } catch(ConfigurationPropertyStoreException e) {
            throw new TestRunException("Problem with the CPS",e);
        }

        testClassWrapper.parseTestClass();

        testClassWrapper.instantiateTestClass();

        if (this.runType == RunType.SHAREDENVIRONMENTBUILD) {
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
                isRunOK = false;
            }
        }

        try {
            generateEnvironment(testClassWrapper, managers);
        } catch(Exception e) {
            logger.fatal("Error within test runner",e);
            this.isRunOK = false;
        }

        if (!isRunOK || this.runType == RunType.TEST || this.runType == RunType.SHAREDENVIRONMENTDISCARD) {
            updateStatus(TestRunLifecycleStatus.ENDING, null);
            managers.endOfTestRun();

            boolean markedWaiting = false;

            if (!resourcesAvailable && !run.isLocal()) {
                markWaiting(this.framework);
                logger.info("Placing queue on the waiting list");
                markedWaiting = true;
            } else {
                if (this.runType == RunType.SHAREDENVIRONMENTDISCARD) {
                    this.testStructure.setResult("Discarded");
                    try {
                        this.dss.deletePrefix("run." + this.run.getName() + ".shared.environment");
                    } catch (DynamicStatusStoreException e) {
                        logger.error("Problem cleaning shared environment properties", e);
                    }
                }
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
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
                deleteRunProperties(this.framework);
            }
        } else if (this.runType == RunType.SHAREDENVIRONMENTBUILD) {
            recordCPSProperties(frameworkInitialisation);
            updateStatus(TestRunLifecycleStatus.UP, "built");
        } else {
            logger.error("Unrecognised end condition");
        }

        managers.shutdown();

        frameworkInitialisation.shutdownFramework();
    }

    private void generateEnvironment(TestClassWrapper testClassWrapper, TestRunManagers managers) throws TestRunException {
        if(isRunOK){
            try {
                updateStatus(TestRunLifecycleStatus.GENERATING, null);
                logger.info("Starting Provision Generate phase");
                managers.provisionGenerate();
                createEnvironment(testClassWrapper, managers);
            } catch (Exception e) { 
                logger.info("Provision Generate failed", e);
                if (e instanceof FrameworkResourceUnavailableException) {
                    this.resourcesAvailable = false;
                }
                testClassWrapper.setResult(Result.envfail(e));
                if (resourcesAvailable) {
                    managers.testClassResult(testClassWrapper.getResult(), e);
                }
                testStructure.setResult(testClassWrapper.getResult().getName());
                isRunOK = false;
            }
        }
    }


    private void createEnvironment(TestClassWrapper testClassWrapper, TestRunManagers managers) throws TestRunException {
        if (!isRunOK) {
            return;
        }

        try {
            if (this.runType == RunType.TEST || this.runType == RunType.SHAREDENVIRONMENTBUILD) {
                try {
                    updateStatus(TestRunLifecycleStatus.BUILDING, null);
                    logger.info("Starting Provision Build phase");
                    managers.provisionBuild();
                } catch (FrameworkException e) {
                    this.isRunOK = false;
                    logger.error("Provision build failed",e);
                    if (e instanceof FrameworkResourceUnavailableException) {
                        this.resourcesAvailable = false;
                    }
                    testClassWrapper.setResult(Result.envfail(e));
                    if (this.resourcesAvailable) {
                        managers.testClassResult(testClassWrapper.getResult(), e);
                    }
                    testStructure.setResult(testClassWrapper.getResult().getName());
                    return;
                }
            }

            runEnvironment(testClassWrapper, managers);
        } finally {
            discardEnvironment(managers);
        }
    }


    private void discardEnvironment(TestRunManagers managers) {
        if (this.runType != RunType.SHAREDENVIRONMENTBUILD) {
            logger.info("Starting Provision Discard phase");
            managers.provisionDiscard();
        }
    }


    private void runEnvironment(TestClassWrapper testClassWrapper, TestRunManagers managers) throws TestRunException {
        if (isRunOK) {    
            try {
                if (this.runType != RunType.SHAREDENVIRONMENTDISCARD) {
                    try {
                        updateStatus(TestRunLifecycleStatus.PROVSTART, null);
                        logger.info("Starting Provision Start phase");
                        managers.provisionStart();
                    } catch (FrameworkException e) {
                        this.isRunOK = false;
                        logger.error("Provision start failed",e);
                        if (e instanceof FrameworkResourceUnavailableException) {
                            this.resourcesAvailable = false;
                        }
                        testClassWrapper.setResult(Result.envfail(e));
                        testStructure.setResult(testClassWrapper.getResult().getName());
                        return;
                    }
                }
                
                runTestClassWrapper(testClassWrapper, managers);
            } finally {
                stopEnvironment(managers);
            }
        }
        return;
    }

    private void stopEnvironment(TestRunManagers managers) {
        if (this.runType == RunType.SHAREDENVIRONMENTBUILD) {   
            logger.info("Starting Provision Stop phase");
            managers.provisionStop();
        }
    }


    private void runTestClassWrapper(TestClassWrapper testClassWrapper, TestRunManagers managers) throws TestRunException {
        if (this.runType == RunType.SHAREDENVIRONMENTBUILD && isRunOK) {
            
            updateStatus(TestRunLifecycleStatus.RUNNING, null);
            try {
                logger.info("Running the test class");
                testClassWrapper.runTestMethods(managers, this.dss, this.run.getName());
            } finally {
                updateStatus(TestRunLifecycleStatus.RUNDONE, null);
            }
        }

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

    private void updateStatus(TestRunLifecycleStatus status, String timestamp) throws TestRunException {

        this.testStructure.setStatus(status.toString());
        if ("finished".equals(status.toString())) {
            updateResult();
            this.testStructure.setEndTime(Instant.now());
        }

        writeTestStructure();

        try {
            this.dss.put("run." + run.getName() + ".status", status.toString());
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

    protected IFramework getFramework() {
        return this.framework;
    }

    public IConfigurationPropertyStoreService getCPS() {
        return this.cps;
    }

    private void recordCPSProperties(FrameworkInitialisation frameworkInitialisation) {
        try {
            Properties record = this.framework.getRecordProperties();

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
            IResultArchiveStore ras = this.framework.getResultArchiveStore();
            Path rasRoot = ras.getStoredArtifactsRoot();
            Path rasProperties = rasRoot.resolve("framework").resolve("cps_record.properties");
            Files.createFile(rasProperties, ResultArchiveStoreContentType.TEXT);
            Files.write(rasProperties, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Failed to save the recorded properties", e);
        }
    }

}
