/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.time.Instant;

import dev.galasa.api.run.Run;

public interface IRun {

    String getName();

    Instant getHeartbeat();

    String getType();

    String getTest();

    String getStatus();

    String getRequestor();

    String getStream();

    String getTestBundleName();

    String getTestClassName();

    boolean isLocal();

    String getGroup();

    Instant getQueued();

    String getRepository();

    String getOBR();

    boolean isTrace();

    Instant getFinished();

    Instant getWaitUntil();

    Run getSerializedRun();

    String getResult();
    
    boolean isSharedEnvironment();
}
