/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.time.Instant;

import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.IRun;

public class MockIRun implements IRun{

    /* IRun newRun = framework.getFrameworkRuns().submitRun(request.getRequestorType(), request.getRequestor(), bundle, testClass,
                        groupName, request.getMavenRepository(), request.getObr(), request.getTestStream(), false,
                        request.isTrace(), request.getOverrides(), 
                        senvPhase, 
                        request.getSharedEnvironmentRunName(),
                        "java");
                         */
    private String runName;
    private String runType;
    private String requestor;
    private String test;
    private String runStatus;
    private String bundle; 
    private String testClass;
    private String groupName;

    
    public MockIRun(String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName){
        this.runName = runName;
        this.runType = runType;
        this.requestor = requestor;
        this.test = test;
        this.runStatus = runStatus;
        this.bundle = bundle;
        this.testClass = testClass;
        this.groupName = groupName;
    }

    @Override
    public String getName() {
        return this.runName;
    }

    @Override
    public Instant getHeartbeat() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeartbeat'");
    }

    @Override
    public String getType() {
        return this.runType;
    }

    @Override
    public String getTest() {
        return this.test;
    }

    @Override
    public String getStatus() {
        return this.runStatus;
    }

    @Override
    public String getRequestor() {
        return this.requestor;
    }

    @Override
    public String getStream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStream'");
    }

    @Override
    public String getTestBundleName() {
       return this.bundle;
    }

    @Override
    public String getTestClassName() {
        return this.testClass;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public String getGroup() {
        return this.groupName;
    }

    @Override
    public Instant getQueued() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQueued'");
    }

    @Override
    public String getRepository() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRepository'");
    }

    @Override
    public String getOBR() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOBR'");
    }

    @Override
    public boolean isTrace() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isTrace'");
    }

    @Override
    public Instant getFinished() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFinished'");
    }

    @Override
    public Instant getWaitUntil() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWaitUntil'");
    }

    @Override
    public Run getSerializedRun() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSerializedRun'");
    }

    @Override
    public String getResult() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }

    @Override
    public boolean isSharedEnvironment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSharedEnvironment'");
    }

    @Override
    public String getGherkin() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGherkin'");
    }
    
}