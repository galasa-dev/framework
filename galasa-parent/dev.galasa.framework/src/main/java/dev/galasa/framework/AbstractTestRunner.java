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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IShuttableFramework;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class AbstractTestRunner {

    private Log logger = LogFactory.getLog(AbstractTestRunner.class);

    protected BundleContext bundleContext;

    protected IShuttableFramework framework;
    protected IBundleManager bundleManager;
    protected IFileSystem fileSystem;

    protected IConfigurationPropertyStoreService cps;
    protected IDynamicStatusStoreService dss;
    protected IResultArchiveStore ras;
    protected IRun run;

    protected TestStructure testStructure = new TestStructure();

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

    protected void recordCPSProperties(
        IFileSystem fileSystem, 
        IFramework framework,
        IResultArchiveStore ras
    ) {
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
            
            Path rasRoot = ras.getStoredArtifactsRoot();
            Path rasProperties = rasRoot.resolve("framework").resolve("cps_record.properties");
            fileSystem.createFile(rasProperties, ResultArchiveStoreContentType.TEXT);
            fileSystem.write(rasProperties, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Failed to save the recorded properties", e);
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
}
