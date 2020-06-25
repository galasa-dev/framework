/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
import dev.galasa.framework.spi.gherkin.GherkinTest;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.DssUtils;

/**
 * Run the supplied test class
 */
@Component(service = { GherkinTestRunner.class })
public class GherkinTestRunner {

    private Log logger = LogFactory.getLog(GherkinTestRunner.class);

    private BundleContext bundleContext;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private RepositoryAdmin repositoryAdmin;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private IMavenRepository mavenRepository;

    private TestRunHeartbeat heartbeat;

    private IConfigurationPropertyStoreService cps;
    private IDynamicStatusStoreService dss;
    private IResultArchiveStore ras;
    private IRun run;

    private TestStructure testStructure = new TestStructure();

    private GherkinTest gherkinTest;

    private boolean runOk = true;
    private boolean resourcesUnavailable = false;

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

        gherkinTest = new GherkinTest(run);

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
            BundleManagement.loadAllManagerBundles(repositoryAdmin, bundleContext);
        } catch (Exception e) {
            logger.error("Unable to load the managers obr", e);
            updateStatus("finished", "finished");
            this.ras.shutdown();
            return;
        }

        if(gherkinTest.getName() == null || gherkinTest.getMethods().size() == 0) {
            throw new TestRunException("Feature file is invalid at URI: " + run.getGherkin());
        }
            
        logger.info("Run test: " + gherkinTest.getName());

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

        updateStatus("started", "started");

        // *** Initialise the Managers ready for the test run
        TestRunManagers managers = null;
        try {
            managers = new TestRunManagers(frameworkInitialisation.getFramework(), gherkinTest);
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

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

}
