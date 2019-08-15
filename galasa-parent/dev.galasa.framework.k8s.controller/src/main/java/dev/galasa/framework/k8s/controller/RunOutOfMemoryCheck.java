package dev.galasa.framework.k8s.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RunOutOfMemoryCheck implements Runnable {
	private final Log logger = LogFactory.getLog(getClass());

	@Override
	public synchronized void run() {
		// TODO Write it!!!!
		logger.info("Not performing the OOM check,   not written yet");
	}
}
