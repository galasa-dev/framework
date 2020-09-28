/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

public interface IBackupCPS {

    boolean initialise(IFramework framework);

    void start();

    void shutdown();
}