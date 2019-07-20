package dev.voras.framework.docker.controller;


import java.time.Instant;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.framework.spi.DynamicStatusStoreException;
import dev.voras.framework.spi.IDynamicStatusStoreService;

public class Heartbeat implements Runnable {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private final Settings settings;
	private final IDynamicStatusStoreService dss;
	
	public Heartbeat(IDynamicStatusStoreService dss, Settings settings) {
		this.dss = dss;
		this.settings = settings;
	}

	@Override
	public void run() {
		Instant time = Instant.now();
		
		HashMap<String, String> props = new HashMap<>();
		props.put("servers.controller." + settings.getPodName() + ".heartbeat", time.toString());
		
		try {
			dss.put(props);
		} catch (DynamicStatusStoreException e) {
			logger.error("Problem logging heartbeat",e);
		}
	}

}
