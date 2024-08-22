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

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IShuttableFramework;

public class AbstractTestRunner {

    private Log logger = LogFactory.getLog(AbstractTestRunner.class);

    protected BundleContext bundleContext;

    protected IShuttableFramework framework;
    protected IBundleManager bundleManager;
    protected IFileSystem fileSystem;

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
}
