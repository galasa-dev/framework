package dev.galasa.framework.resource.management.internal;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;

public class RunRASMonitor implements Runnable {

    private final IResourceManagement                   resourceManagement;
    private final IFramework                            framework;
    private final IResultArchiveStore                   ras;
    private final IDynamicStatusStoreService            dss;
    private final IConfigurationPropertyStoreService    cps;

    private final Log logger = LogFactory.getLog(this.getClass());

    protected RunRASMonitor(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, IConfigurationPropertyStoreService cps,
            RunResourceManagement runResourceManagement) throws FrameworkException{
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.cps = cps;
        this.ras = framework.getResultArchiveStore();
    }

    @Override
    public void run() {
        logger.info("Starting search for old Runs");

        try {
            int amount = cps.getProperty("ras", "cleanup.days");

            Instant now = Instant.now();
            Instant requestUpTo = now.minus(amount, ChronoUnit.DAYS);

            IResultArchiveStore ras = framework.getResultArchiveStore();
            for(IResultArchiveStoreDirectoryService service :  ras.getDirectoryServices()) {
                for (IRunResult run : service.getRuns(new RasSearchCriteriaQueuedTo(requestUpTo))) {
                    logger.info("Discarding old run: " + run.getTestStructure().getRunName());
                    run.discard();
                }
            }
        } catch (Exception e) {
            logger.error("RAS cleanup check failed", e);
        }        
    }
}
