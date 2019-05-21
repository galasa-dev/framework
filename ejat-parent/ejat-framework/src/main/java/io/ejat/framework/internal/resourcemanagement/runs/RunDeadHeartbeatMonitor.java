package io.ejat.framework.internal.resourcemanagement.runs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.ejat.framework.spi.AbstractManager;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IResourceManagement;

public class RunDeadHeartbeatMonitor implements Runnable {

	private final IResourceManagement        resourceManagement;
	private final IDynamicStatusStoreService dss;
	private final IConfigurationPropertyStoreService cps;
	private final Log                        logger = LogFactory.getLog(this.getClass());
	private final Pattern                    runHeartbeatPattern = Pattern.compile("^\\Qrun.\\E(\\w+)\\Q.heartbeat\\E$");

	private final DateTimeFormatter           dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(ZoneId.systemDefault());	

	protected RunDeadHeartbeatMonitor(IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss, RunResourceManagement runResourceManagement, IConfigurationPropertyStoreService cps) {
		this.resourceManagement = resourceManagement;
		this.dss = dss;
		this.cps = cps;
		this.logger.info("Run Dead Heartbeat Monitor initialised");
	}

	@Override
	public void run() {
		int defaultDeadHeartbeatTime = 300;  //** 5 minutes 
		try {
			String overrideTime = AbstractManager.nulled(cps.getProperty("resource.management", "dead.heartbeat.timeout"));
			if (overrideTime != null) {
				defaultDeadHeartbeatTime = Integer.parseInt(overrideTime);
			}
		} catch(Exception e) {
			logger.error("Problem with resource.management.dead.heartbeat.timeout, using default " + defaultDeadHeartbeatTime,e);
		}

		logger.info("Starting Run Dead Heartbeat search");
		try {
			Map<String, String> runProperties = this.dss.getPrefix("run.");

			for(Entry<String, String> entry : runProperties.entrySet()) {
				try {
					String key = entry.getKey();
					Matcher matcher = runHeartbeatPattern.matcher(key);
					if (matcher.find()) {
						String runName = matcher.group(1);

						Instant heartbeat = Instant.parse(entry.getValue());
						Instant expires = heartbeat.plusSeconds(defaultDeadHeartbeatTime);
						Instant now = Instant.now();
						if (expires.compareTo(now) <= 0) {
							///put time management into the framework
							String lastHeartbeat = dtf.format(LocalDateTime.ofInstant(heartbeat, ZoneId.systemDefault()));
							logger.warn("Deleting run " + runName + ", last heartbeat was at " + lastHeartbeat);

							dss.deletePrefix("run." + runName + ".");
						}
					}
				} catch(Exception e) {
					logger.error("Scan of run property failed",e);
				}
			}
		} catch (DynamicStatusStoreException e) {
			logger.error("Scan of runs failed", e);
		}

		this.resourceManagement.resourceManagementRunSuccessful();
		logger.info("Finished Run Dead Heartbeat search");
	}

}
