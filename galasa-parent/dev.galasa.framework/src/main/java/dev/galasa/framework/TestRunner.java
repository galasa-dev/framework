/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import dev.galasa.framework.internal.runner.FelixRepoAdminOBRAdder;
import dev.galasa.framework.internal.runner.MavenRepositoryListBuilder;
import dev.galasa.framework.internal.runner.RunType;
import dev.galasa.framework.internal.runner.RunTypeDetails;
import dev.galasa.framework.internal.runner.TestRunnerDataProvider;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.FrameworkResourceUnavailableException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaTest;

/**
 * Run the supplied test class
 */
@Component(service = { TestRunner.class })
public class TestRunner extends BaseTestRunner {


    private Log                                logger        = LogFactory.getLog(TestRunner.class);

    // Field is protected so unit tests can inject a value here.
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected RepositoryAdmin                    repositoryAdmin;

    // Field is protected so unit tests can inject a value here.
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected IMavenRepository                   mavenRepository;

    private RunType                            runType;


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

        String testBundleName = run.getTestBundleName();
        String testClassName = run.getTestClassName();

        this.testStructure = createNewTestStructure(run);
        writeTestStructure();
            
        try {

            String rasRunId = this.ras.calculateRasRunId();
            storeRasRunIdInDss(dss, rasRunId);

            Class<?> testClass ;

            try {
                
                String streamName = AbstractManager.nulled(run.getStream());
                new MavenRepositoryListBuilder(this.mavenRepository, this.cps)
                    .addMavenRepositories(streamName, run.getRepository());
                new FelixRepoAdminOBRAdder(this.repositoryAdmin, this.cps)
                    .addOBRsToRepoAdmin(streamName, run.getOBR());


                // This is java-test-runner-specific
                loadTestBundle(repositoryAdmin, bundleContext, testBundleName);
                testClass = getTestClass(bundleContext, testBundleName, testClassName);


            } catch (Exception ex) {
                updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                throw new TestRunException(ex.getMessage(),ex);
            }


            RunTypeDetails runTypeDetails = new RunTypeDetails(dataProvider.getAnnotationExtractor(), testClass, testBundleName, testClassName , framework);
            this.runType = runTypeDetails.getDetectedRunType();

            logger.debug("Test runType is "+this.runType.toString());
            if (this.runType == RunType.TEST) {

                heartbeat = createBeatingHeart(framework);

                incrimentMetric(dss,run);


            } else if (this.runType == RunType.SHARED_ENVIRONMENT_BUILD) {
                int expireHours = runTypeDetails.getSharedEnvironmentExpireAfterHours();
                Instant expire = Instant.now().plus(expireHours, ChronoUnit.HOURS);
                try {
                    this.dss.put("run." + this.run.getName() + ".shared.environment.expire", expire.toString());
                } catch (DynamicStatusStoreException e) {
                    String msg = "DynamicStatusStoreException Exception caught. "+e.getMessage()+" Shutting down and Re-throwing.";
                    logger.error(msg);
                    deleteRunProperties(this.framework);
                    throw new TestRunException("Unable to set the shared environment expire time",e);
                }
            }

            logger.debug("state changing to started.");
            updateStatus(TestRunLifecycleStatus.STARTED, "started");

            // *** Try to load the Core Manager bundle, even if the test doesn't use it, and if not already active
            if (!bundleManager.isBundleActive(bundleContext, "dev.galasa.core.manager")) {
                try {
                    bundleManager.loadBundle(repositoryAdmin, bundleContext, "dev.galasa.core.manager");
                } catch (FrameworkException e) {
                    logger.warn("Tried to load the Core Manager bundle, but failed, test can continue without it",e);
                }
            }

            logger.debug("Bundle is loaded ok.");

            // *** Initialise the Managers ready for the test run
            ITestRunManagers managers = null;
            try {
                GalasaTest galasaTest = new GalasaTest(testClass);
                managers = dataProvider.createTestRunManagers(galasaTest);
            } catch (TestRunException e) {
                String msg = "Exception Exception caught. "+e.getMessage()+" Shutting down and Re-throwing.";
                logger.error(msg);
                throw new TestRunException("Problem initialising the Managers for a test run", e);
            }

            logger.debug("Test managers ok.");

            try {
                if (managers.anyReasonTestClassShouldBeIgnored()) {
                    logger.debug("managers.anyReasonTestClassShouldBeIgnored() is true. Shutting down.");
                    stopHeartbeat();
                    updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                    return; // TODO handle ignored classes
                }
            } catch (FrameworkException e) {
                String msg = "Problem asking Managers for an ignore reason";
                logger.error(msg+" "+e.getMessage());
                throw new TestRunException(msg, e);
            }
            logger.debug("Test class should not be ignored.");

            
            TestClassWrapper testClassWrapper;
            try { 
                
                testClassWrapper = new TestClassWrapper(this, testBundleName, testClass, testStructure);
            } catch(ConfigurationPropertyStoreException e) {
                String msg = "Problem with the CPS when adding a wrapper";
                logger.error(msg+" "+e.getMessage());
                throw new TestRunException(msg,e);
            }

            logger.debug("Parsing test class...");
            testClassWrapper.parseTestClass();

            logger.debug("Instantiating test class...");
            testClassWrapper.instantiateTestClass();

            if (this.runType == RunType.SHARED_ENVIRONMENT_BUILD) {
                logger.debug("Checking active managers to see if they support shared env build...");
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
            logger.debug("isRunOK: "+Boolean.toString(isRunOK));

            logger.debug("Generating environment...");
            try {
                generateEnvironment(testClassWrapper, managers, this.dss, this.run.getName() , isRunOK);
            } catch(Exception e) {
                logger.fatal("Error within test runner",e);
                this.isRunOK = false;
            }

            logger.debug("isRunOK: "+Boolean.toString(isRunOK)+" runType: "+runType.toString());

            if (!isRunOK || this.runType == RunType.TEST || this.runType == RunType.SHARED_ENVIRONMENT_DISCARD) {
                logger.debug("Test did not run OK... or runtype is not "+RunType.SHARED_ENVIRONMENT_BUILD.toString());
                updateStatus(TestRunLifecycleStatus.ENDING, null);
                managers.endOfTestRun();

                boolean markedWaiting = false;

                if (!isResourcesAvailable && !run.isLocal()) {
                    markWaiting(this.framework);
                    logger.info("Placing queue on the waiting list");
                    markedWaiting = true;
                } else {
                    if (this.runType == RunType.SHARED_ENVIRONMENT_DISCARD) {
                        this.testStructure.setResult("Discarded");
                        try {
                            this.dss.deletePrefix("run." + this.run.getName() + ".shared.environment");
                        } catch (DynamicStatusStoreException e) {
                            logger.error("Problem cleaning shared environment properties", e);
                        }
                    }
                    updateStatus(TestRunLifecycleStatus.FINISHED, "finished");
                }

                logger.debug("Stopping heartbeat...");
                stopHeartbeat();

                // Record all the CPS properties that were accessed
                saveUsedCPSPropertiesToArtifact(this.framework.getRecordProperties(), this.fileSystem, this.ras);
                // And all the overrides the test was passed.
                saveAllOverridesPassedToArtifact(overrideProperties, this.fileSystem , this.ras);

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
            } else if (this.runType == RunType.SHARED_ENVIRONMENT_BUILD) {

                // Record all the CPS properties that were accessed
                saveUsedCPSPropertiesToArtifact(this.framework.getRecordProperties(), this.fileSystem, this.ras);
                // And all the overrides the test was passed.
                saveAllOverridesPassedToArtifact(overrideProperties, this.fileSystem , this.ras);
                
                updateStatus(TestRunLifecycleStatus.UP, "built");
            } else {
                logger.error("Unrecognised end condition");
            }

            logger.debug("Cleaning up managers...");
            managers.shutdown();

        } finally {
            logger.debug("Cleaning up framework...");
            shutdownFramework(framework);
        }
    }



    private void generateEnvironment(TestClassWrapper testClassWrapper, ITestRunManagers managers, IDynamicStatusStoreService dss, String runName , boolean isRunOK) throws TestRunException {
        if(isRunOK){
            try {
                updateStatus(TestRunLifecycleStatus.GENERATING, null);
                logger.info("Starting Provision Generate phase");
                managers.provisionGenerate();
                createEnvironment(testClassWrapper, managers, dss, runName, isRunOK);
            } catch (Exception e) { 
                logger.info("Provision Generate failed", e);
                if (e instanceof FrameworkResourceUnavailableException) {
                    this.isResourcesAvailable = false;
                }
                testClassWrapper.setResult(Result.envfail(e));
                if (isResourcesAvailable) {
                    managers.testClassResult(testClassWrapper.getResult(), e);
                }
                testStructure.setResult(testClassWrapper.getResult().getName());
                isRunOK = false;
            }
        }
    }


    private void createEnvironment(
        TestClassWrapper testClassWrapper, 
        ITestRunManagers managers, 
        IDynamicStatusStoreService dss, 
        String runName, 
        boolean isRunOK
    ) throws TestRunException {

        if (isRunOK) {

            try {
                if (this.runType == RunType.TEST || this.runType == RunType.SHARED_ENVIRONMENT_BUILD) {
                    try {
                        updateStatus(TestRunLifecycleStatus.BUILDING, null);
                        logger.info("Starting Provision Build phase");
                        managers.provisionBuild();
                    } catch (FrameworkException e) {
                        this.isRunOK = false;
                        logger.error("Provision build failed",e);
                        if (e instanceof FrameworkResourceUnavailableException) {
                            this.isResourcesAvailable = false;
                        }
                        testClassWrapper.setResult(Result.envfail(e));
                        if (this.isResourcesAvailable) {
                            managers.testClassResult(testClassWrapper.getResult(), e);
                        }
                        testStructure.setResult(testClassWrapper.getResult().getName());
                        return;
                    }
                }

                runEnvironment(testClassWrapper, managers, dss, runName);
            } finally {
                discardEnvironment(managers);
            }
        }
    }


    private void discardEnvironment(ITestRunManagers managers) {
        if (this.runType != RunType.SHARED_ENVIRONMENT_BUILD) {
            logger.info("Starting Provision Discard phase");
            managers.provisionDiscard();
        }
    }


    private void runEnvironment(TestClassWrapper testClassWrapper, ITestRunManagers managers, IDynamicStatusStoreService dss , String runName) throws TestRunException {
        if (isRunOK) {    
            try {
                if (this.runType != RunType.SHARED_ENVIRONMENT_DISCARD) {
                    try {
                        updateStatus(TestRunLifecycleStatus.PROVSTART, null);
                        logger.info("Starting Provision Start phase");
                        managers.provisionStart();
                    } catch (FrameworkException e) {
                        this.isRunOK = false;
                        logger.error("Provision start failed",e);
                        if (e instanceof FrameworkResourceUnavailableException) {
                            this.isResourcesAvailable = false;
                        }
                        testClassWrapper.setResult(Result.envfail(e));
                        testStructure.setResult(testClassWrapper.getResult().getName());
                        return;
                    }
                }

                runTestClassWrapper(testClassWrapper, managers, dss, runName); 
            } finally {
                stopEnvironment(managers);
            }
        }
        return;
    }

    private void stopEnvironment(ITestRunManagers managers) {
        if (this.runType != RunType.SHARED_ENVIRONMENT_BUILD) {   
            logger.info("Starting Provision Stop phase");
            managers.provisionStop();
        }
    }


    private void runTestClassWrapper(TestClassWrapper testClassWrapper, ITestRunManagers managers, IDynamicStatusStoreService dss , String runName) throws TestRunException {
        // Do nothing if the test run has already failed on setup.
        if (isRunOK) {

            // Do nothing if we are setting up the shared environment
            if (this.runType != RunType.SHARED_ENVIRONMENT_BUILD ) {
                
                updateStatus(TestRunLifecycleStatus.RUNNING, null);
                try {
                    logger.info("Running the test class");
                    testClassWrapper.runTestMethods(managers, dss, runName);
                } finally {
                    updateStatus(TestRunLifecycleStatus.RUNDONE, null);
                }
            }
        }
    }

    /**
     * Get the test class from the supplied bundle
     * 
     * @param testBundleName
     * @param testClassName
     * @return The test class from the bundle in the bundle context.
     * @throws TestRunException
     */
    private Class<?> getTestClass(BundleContext bundleContext, String testBundleName, String testClassName) throws TestRunException {
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

    private void loadTestBundle(RepositoryAdmin repositoryAdmin, BundleContext bundleContext, String testBundleName) throws TestRunException {
        try {
            this.bundleManager.loadBundle(repositoryAdmin, bundleContext, testBundleName);
        } catch (Exception e) {
            logger.error("Unable to load the test bundle " + testBundleName, e);
            throw new TestRunException("Unable to load the test bundle " + testBundleName, e);
        }
    }

}