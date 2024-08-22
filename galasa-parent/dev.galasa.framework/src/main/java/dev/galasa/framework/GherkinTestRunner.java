/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import dev.galasa.framework.internal.runner.TestRunnerDataProvider;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.FrameworkResourceUnavailableException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.language.gherkin.GherkinMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinTest;
import dev.galasa.framework.spi.utils.DssUtils;


/**
 * Run the supplied test class
 */
@Component(service = { GherkinTestRunner.class })
public class GherkinTestRunner extends AbstractTestRunner {

    private Log logger = LogFactory.getLog(GherkinTestRunner.class);


    // Field is protected so unit tests can inject a value here.
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected RepositoryAdmin repositoryAdmin;

    // Field is protected so unit tests can inject a value here.
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected IMavenRepository mavenRepository;
 

    private GherkinTest gherkinTest;

    /**
     * Run the supplied test class
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @throws TestRunException
     */
    public void runTest(Properties bootstrapProperties, Properties overrideProperties) throws TestRunException {
        TestRunnerDataProvider data = new TestRunnerDataProvider(bootstrapProperties, overrideProperties);
        runTest(data);
    }

    public void runTest( ITestRunnerDataProvider dataProvider  ) throws TestRunException {

        super.init(dataProvider);

        gherkinTest = new GherkinTest(run, testStructure,this.fileSystem);

        String testRepository = null;
        String testOBR = null;
        String stream = AbstractManager.nulled(run.getStream());

        this.testStructure = createNewTestStructure(run);
        this.testStructure.setTestName(gherkinTest.getName());
        writeTestStructure();

        try {

            if (stream != null) {
                logger.debug("Loading test stream " + stream);
                try {
                    testRepository = this.cps.getProperty("test.stream", "repo", stream);
                    testOBR = this.cps.getProperty("test.stream", "obr", stream);
                } catch (Exception e) {
                    logger.error("Unable to load stream " + stream + " settings", e);
                    updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                    return;
                }
            }

            testRepository = getOverriddenValue(testRepository, run.getRepository());
            testOBR = getOverriddenValue(testOBR, run.getOBR());

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
                    return;
                }
            }

            try {
                bundleManager.loadAllGherkinManagerBundles(repositoryAdmin, bundleContext);
            } catch (Exception e) {
                logger.error("Unable to load the managers obr", e);
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                return;
            }

            if(gherkinTest.getName() == null || gherkinTest.getMethods().size() == 0) {
                throw new TestRunException("Feature file is invalid at URI: " + run.getGherkin());
            }
                
            logger.info("Run test: " + gherkinTest.getName());

            try {
                heartbeat = new TestRunHeartbeat(framework);
                heartbeat.start();
            } catch (DynamicStatusStoreException e1) {
                throw new TestRunException("Unable to initialise the heartbeat");
            }

            if (run.isLocal()) {
                DssUtils.incrementMetric(dss, "metrics.runs.local");
            } else {
                DssUtils.incrementMetric(dss, "metrics.runs.automated");
            }

            updateStatus(TestRunLifecycleStatus.STARTED, "started");

            // *** Initialise the Managers ready for the test run
            ITestRunManagers managers = null;
            try {
                managers = dataProvider.createTestRunManagers(new GalasaTest(gherkinTest));
            } catch (TestRunException e) {
                String msg = "Exception Exception caught. "+e.getMessage()+" Shutting down and Re-throwing.";
                logger.error(msg);
                throw new TestRunException("Problem initialising the Managers for a test run", e);
            }

            if(!gherkinTest.allMethodsRegistered()) {
                logStatementsNotRecognisedByAnyManager(gherkinTest);

                stopHeartbeat();
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                throw new TestRunException("Not all methods in test are registered to a Manager");
            }

            try {
                if (managers.anyReasonTestClassShouldBeIgnored()) {
                    stopHeartbeat();
                    updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                    return; // TODO handle ignored classes
                }
            } catch (FrameworkException e) {
                throw new TestRunException("Problem asking Managers for an ignore reason", e);
            }

            try {
                generateEnvironment(gherkinTest, managers);
            } catch(Exception e) {
                logger.fatal("Error within test runner",e);
                isRunOK = false;
            }

            updateStatus(TestRunLifecycleStatus.ENDING, null);
            managers.endOfTestRun();

            boolean markedWaiting = false;

            if ((!isResourcesAvailable) && !run.isLocal()) {
                markWaiting(this.framework);
                logger.info("Placing queue on the waiting list");
                markedWaiting = true;
            } else {
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
            }

            stopHeartbeat();

            // *** Record all the CPS properties that were accessed
            recordCPSProperties(this.fileSystem, this.framework, this.ras);

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
                deleteRunProperties(framework);
            }

            managers.shutdown();
        } finally {
            shutdownFramework(framework);
        }

        return;
    }

    private void logStatementsNotRecognisedByAnyManager(GherkinTest gherkinTest) {
        logger.error("The following Gherkin statements have not been registered to a Manager");
        
        for(GherkinMethod scenario : gherkinTest.getMethods()) {
            logger.info("    Scenario: " + scenario.getName());
            for(IGherkinExecutable executable : scenario.getExecutables()) {
                Object owner = executable.getOwner();
                if (owner != null) {
                    logger.info("        OK - " + executable.getKeyword() + " " + executable.getValue());
                } else {
                    logger.error("        MISSING - " + executable.getKeyword() + " " + executable.getValue());
                }
            }
        }
    }

    private void generateEnvironment(GherkinTest testObject, ITestRunManagers managers) throws TestRunException {
        if (!isRunOK) {
            return;
        }

        try {
            updateStatus(TestRunLifecycleStatus.GENERATING, null);
            logger.info("Starting Provision Generate phase");
            managers.provisionGenerate();
        } catch (Exception e) { 
            logger.info("Provision Generate failed", e);
            if (e instanceof FrameworkResourceUnavailableException) {
                isResourcesAvailable = false;
            }
            testObject.setResult(Result.envfail(e));
            testStructure.setResult(testObject.getResult().getName());
            isRunOK = false;
            return;
        }

        createEnvironment(testObject, managers);
    }


    private void createEnvironment(GherkinTest testObject, ITestRunManagers managers) throws TestRunException {
        if (!isRunOK) {
            return;
        }

        try {
            try {
                updateStatus(TestRunLifecycleStatus.BUILDING, null);
                logger.info("Starting Provision Build phase");
                managers.provisionBuild();
            } catch (FrameworkException e) {
                this.isRunOK = false;
                logger.error("Provision build failed",e);
                if (e instanceof FrameworkResourceUnavailableException) {
                    isResourcesAvailable = false;
                }
                testObject.setResult(Result.envfail(e));
                testStructure.setResult(testObject.getResult().getName());
                return;
            }

            runEnvironment(testObject, managers);
        } finally {
            discardEnvironment(managers);
        }
    }


    private void discardEnvironment(ITestRunManagers managers) {
        logger.info("Starting Provision Discard phase");
        managers.provisionDiscard();
    }


    private void runEnvironment(GherkinTest testObject, ITestRunManagers managers) throws TestRunException {
        if (!isRunOK) {
            return;
        }

        try {
            try {
                updateStatus(TestRunLifecycleStatus.PROVSTART, null);
                logger.info("Starting Provision Start phase");
                managers.provisionStart();
            } catch (FrameworkException e) {
                this.isRunOK = false;
                logger.error("Provision start failed",e);
                if (e instanceof FrameworkResourceUnavailableException) {
                    isResourcesAvailable = false;
                }
                testObject.setResult(Result.envfail(e));
                testStructure.setResult(testObject.getResult().getName());
                return;
            }

            runGherkinTest(testObject, managers);
        } finally {
            stopEnvironment(managers);
        }
    }

    private void stopEnvironment(ITestRunManagers managers) {
        logger.info("Starting Provision Stop phase");
        managers.provisionStop();
    }


    private void runGherkinTest(GherkinTest testObject, ITestRunManagers managers) throws TestRunException {
        if (!isRunOK) {
            return;
        }

        updateStatus(TestRunLifecycleStatus.RUNNING, null);
        try {
            logger.info("Running the test class");
            testObject.runTestMethods(managers);
        } finally {
            updateStatus(TestRunLifecycleStatus.RUNDONE, null);
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
        properties.put(getDSSKeyString("status"), "waiting");
        properties.put(getDSSKeyString("wait.until"), until.toString());
        try {
            this.dss.put(properties);
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Unable to place run in waiting state", e);
        }
    }

    private void updateStatus(TestRunLifecycleStatus status, String timestamp) throws TestRunException {
        Instant time = Instant.now();

        this.testStructure.setStatus(status.toString());
        if (status == TestRunLifecycleStatus.FINISHED) {
            updateResult();
            this.testStructure.setEndTime(time);
        }

        writeTestStructure();

        try {
            this.dss.put(getDSSKeyString("status"), status.toString());
            if (timestamp != null) {
                this.dss.put(getDSSKeyString(timestamp), time.toString());
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
            this.dss.put(getDSSKeyString("result"), this.testStructure.getResult());
        } catch (DynamicStatusStoreException e) {
            throw new TestRunException("Failed to update result", e);
        }
    }



    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }



}