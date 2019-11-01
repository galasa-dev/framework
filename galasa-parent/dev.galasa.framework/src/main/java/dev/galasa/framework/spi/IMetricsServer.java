/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.util.concurrent.ScheduledExecutorService;

public interface IMetricsServer {

    ScheduledExecutorService getScheduledExecutorService();

    void metricsPollSuccessful();
}
