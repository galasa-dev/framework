package io.ejat.framework;

import java.time.Instant;
import java.util.Map;

import io.ejat.framework.spi.IRun;

public class RunImpl implements IRun {
	
	private final String name;
	private final Instant heartbeat;
	
	public RunImpl(String name, Map<String, String> runProperties) {
		this.name      = name;
		
		String prefix = "run." + name + ".";
		
		String sHeartbeat = runProperties.get(prefix + "heartbeat");
		if (sHeartbeat != null) {
			this.heartbeat = Instant.parse(sHeartbeat);
		} else {
			this.heartbeat = null;
		}
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Instant getHeartbeat() {
		return this.heartbeat;
	}
	
}
