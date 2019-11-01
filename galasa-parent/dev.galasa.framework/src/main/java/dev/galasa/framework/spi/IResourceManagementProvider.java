/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

public interface IResourceManagementProvider {

    boolean initialise(IFramework framework, IResourceManagement resourceManagement) throws ResourceManagerException;

    void start();

    void shutdown();

    void runFinishedOrDeleted(String runName);
}
