/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.FrameworkResourceUnavailableException;
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

    private BundleContext bundleContext;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private RepositoryAdmin repositoryAdmin;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private IMavenRepository mavenRepository;

    private GherkinTest gherkinTest;

    /**
     * Run the supplied test class
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @throws TestRunException
     */
    public void runTest(Properties bootstrapProperties, Properties overrideProperties) throws TestRunException {

        super.init(bootstrapProperties, overrideProperties);


        gherkinTest = new GherkinTest(run, testStructure);

        loadOverrideProperties(overrideProperties);

        setUnknownTestState(run);
        allocateRasRunId();

        try {
            BundleManagement.loadAllGherkinManagerBundles(repositoryAdmin, bundleContext);

            if(gherkinTest.getName() == null || gherkinTest.getMethods().size() == 0) {
                throw new TestRunException("Feature file is invalid at URI: " + run.getGherkin());
            }

            logger.info("Run test: " + gherkinTest.getName());

            try {
                heartbeat = new TestRunHeartbeat(frameworkInitialisation.getFramework());
                heartbeat.start();
            } catch (DynamicStatusStoreException e1) {
                throw new TestRunException("Unable to initialise the heartbeat");
            }

        } catch (Exception e) {
            updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
            frameworkInitialisation.shutdownFramework();
            return;
        }

        if (run.isLocal()) {
            DssUtils.incrementMetric(dss, "metrics.runs.local");
        } else {
            DssUtils.incrementMetric(dss, "metrics.runs.automated");
        }

        updateStatus(TestRunLifecycleStatus.STARTED, "started");

        // *** Initialise the Managers ready for the test run
        TestRunManagers managers = null;
        try {
            managers = new TestRunManagers(frameworkInitialisation.getFramework(), new GalasaTest(gherkinTest));
        } catch (FrameworkException e) {
            frameworkInitialisation.shutdownFramework();
            throw new TestRunException("Problem initialising the Managers for a test run", e);
        }

        if(!gherkinTest.allMethodsRegistered()) {
            logStatementsNotRecognisedByAnyManager(gherkinTest);

            stopHeartbeat();
            updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
            frameworkInitialisation.shutdownFramework();
            throw new TestRunException("Not all methods in test are registered to a Manager");
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

        try {
            generateEnvironment(gherkinTest, managers);
        } catch(Exception e) {
            logger.fatal("Error within test runner",e);
            this.isRunOK = false;
        }

        updateStatus(TestRunLifecycleStatus.ENDING, null);
        managers.endOfTestRun();

        boolean markedWaiting = false;

        if ((!resourcesAvailable) && !run.isLocal()) {
            markWaiting(frameworkInitialisation.getFramework());
            logger.info("Placing queue on the waiting list");
            markedWaiting = true;
        } else {
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
            deleteRunProperties(frameworkInitialisation.getFramework());
        }

        managers.shutdown();

        frameworkInitialisation.shutdownFramework();

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

    private void generateEnvironment(GherkinTest testObject, TestRunManagers managers) throws TestRunException {
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
                this.resourcesAvailable = false;
            }
            testObject.setResult(Result.envfail(e));
            testStructure.setResult(testObject.getResult().getName());
            isRunOK = false;
            return;
        }

        createEnvironment(testObject, managers);
    }


    private void createEnvironment(GherkinTest testObject, TestRunManagers managers) throws TestRunException {
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
                    this.resourcesAvailable = false;
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


    private void discardEnvironment(TestRunManagers managers) {
        logger.info("Starting Provision Discard phase");
        managers.provisionDiscard();
    }


    private void runEnvironment(GherkinTest testObject, TestRunManagers managers) throws TestRunException {
        if (isRunOK) {
            try {
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
                    testObject.setResult(Result.envfail(e));
                    testStructure.setResult(testObject.getResult().getName());
                    return;
                }

                runGherkinTest(testObject, managers);
            } finally {
                stopEnvironment(managers);
            }
        }
    }

    private void stopEnvironment(TestRunManagers managers) {
        logger.info("Starting Provision Stop phase");
        managers.provisionStop();
    }


    private void runGherkinTest(GherkinTest testObject, TestRunManagers managers) throws TestRunException {
        if (isRunOK) {

            updateStatus(TestRunLifecycleStatus.RUNNING, null);
            try {
                logger.info("Running the test class");
                testObject.runTestMethods(managers);
            } finally {
                updateStatus(TestRunLifecycleStatus.RUNDONE, null);
            }
        }
    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

}