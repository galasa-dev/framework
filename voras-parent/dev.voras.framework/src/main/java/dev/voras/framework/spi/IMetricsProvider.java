package dev.voras.framework.spi;

public interface IMetricsProvider {
	
	boolean initialise(IFramework framework, IMetricsServer metricsServer) throws MetricsServerException;
	void start();
	void shutdown();
}