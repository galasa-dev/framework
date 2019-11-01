/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.docker.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import dev.galasa.framework.docker.controller.pojo.Container;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;

public class RunDeleted implements Runnable {
    private final Log                 logger = LogFactory.getLog(getClass());

    private final Settings            settings;
    private final CloseableHttpClient httpClient;
    private final IFrameworkRuns      runs;

    public RunDeleted(Settings settings, CloseableHttpClient httpClient, IFrameworkRuns runs) {
        this.settings = settings;
        this.httpClient = httpClient;
        this.runs = runs;
    }

    @Override
    public void run() {
        logger.info("Starting Deleted runs scan");

        try {
            List<Container> containers = RunPoll.getContainers(httpClient, settings);
            RunPoll.filterTerminated(containers);

            for (Container container : containers) {
                String runName = container.Labels.galasaRun;
                if (runName == null) {
                    continue;
                }

                IRun run = runs.getRun(runName);
                if (run != null) {
                    continue;
                }

                logger.info("Deleting container " + container.Names.get(0) + " as run has been deleted");
                deleteContainer(container);
            }

        } catch (Exception e) {
            logger.error("Problem with Deleted runs scan", e);
        }

    }

    private void deleteContainer(Container container) {
        try {
            String containerName = container.Names.get(0);
            logger.info("Deleting container " + containerName + " id " + container.Id);
            HttpDelete Delete = new HttpDelete(settings.getDockerUrl().toString() + "/containers/" + container.Id);
            try (CloseableHttpResponse response = httpClient.execute(Delete)) {
                StatusLine status = response.getStatusLine();
                EntityUtils.consume(response.getEntity());

                if (status.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                    throw new DockerControllerException("Delete container failed - " + status);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete engine pod", e);
        }
    }

}
