/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

public interface IMetricsProvider {

    boolean initialise(IFramework framework, IMetricsServer metricsServer) throws MetricsServerException;

    void start();

    void shutdown();
}
