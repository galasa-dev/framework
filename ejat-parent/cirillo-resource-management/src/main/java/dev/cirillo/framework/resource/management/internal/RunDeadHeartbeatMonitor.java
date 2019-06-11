package dev.cirillo.framework.resource.management.internal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.ejat.framework.spi.AbstractManager;
import io.ejat.framework.spi.FrameworkException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkRuns;
import io.ejat.framework.spi.IResourceManagement;
import io.ejat.framework.spi.IRun;

public class RunDeadHeartbeatMonitor implements Runnable {

	private final IResourceManagement        resourceManagement;
	private final IConfigurationPropertyStoreService cps;
	private final IFrameworkRuns             frameworkRuns;
	private final Log                        logger = LogFactory.getLog(this.getClass());

	private final DateTimeFormatter           dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(ZoneId.systemDefault());	

	protected RunDeadHeartbeatMonitor(IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss, RunResourceManagement runResourceManagement, IConfigurationPropertyStoreService cps) throws FrameworkException {
		this.resourceManagement = resourceManagement;
		this.frameworkRuns = framework.getFrameworkRuns();
		this.cps = cps;
		this.logger.info("Run Dead Heartbeat Monitor initialised");
	}

	@Override
	public void run() {
		int defaultDeadHeartbeatTime = 300;  //** 5 minutes 
		try { // TODO do we need a different timeout for automation run reset?
			String overrideTime = AbstractManager.nulled(cps.getProperty("resource.management", "dead.heartbeat.timeout"));
			if (overrideTime != null) {
				defaultDeadHeartbeatTime = Integer.parseInt(overrideTime);
			}
		} catch(Exception e) {
			logger.error("Problem with resource.management.dead.heartbeat.timeout, using default " + defaultDeadHeartbeatTime,e);
		}

		logger.info("Starting Run Dead Heartbeat search");
		try {
			List<IRun> runs = frameworkRuns.getActiveRuns();
			for(IRun run : runs) {
				String runName = run.getName();

				Instant heartbeat = run.getHeartbeat();
				Instant expires = heartbeat.plusSeconds(defaultDeadHeartbeatTime);
				Instant now = Instant.now();
				if (expires.compareTo(now) <= 0) {
					String lastHeartbeat = dtf.format(LocalDateTime.ofInstant(heartbeat, ZoneId.systemDefault()));
					if (run.isLocal()) {
						///TODO put time management into the framework
						logger.warn("Deleting run " + runName + ", last heartbeat was at " + lastHeartbeat);
						this.frameworkRuns.delete(runName);
					} else {
						logger.warn("Reseting run " + runName + ", last heartbeat was at " + lastHeartbeat);
						this.frameworkRuns.reset(runName);
					}
				}
			}
		} catch (FrameworkException e) {
			logger.error("Scan of runs failed", e);
		}

		this.resourceManagement.resourceManagementRunSuccessful();
		logger.info("Finished Run Dead Heartbeat search");
	}

}
