package io.ejat.framework.spi;

import java.util.concurrent.ScheduledExecutorService;

public interface IMetricsServer {
	
	ScheduledExecutorService getScheduledExecutorService();
	
	void metricsPollSuccessful();
}
