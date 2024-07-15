/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.time.Instant;


import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.IRun;

public class MockRun implements IRun {

    private String gherkinFeatureText;

    @Override
    public String getGherkin() {
        return gherkinFeatureText;
    }

    public void setGherkin(String featureText) {
        this.gherkinFeatureText = featureText;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public Instant getHeartbeat() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeartbeat'");
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    @Override
    public String getTest() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTest'");
    }

    @Override
    public String getStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
    }

    @Override
    public String getRequestor() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRequestor'");
    }

    @Override
    public String getStream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStream'");
    }

    @Override
    public String getTestBundleName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTestBundleName'");
    }

    @Override
    public String getTestClassName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTestClassName'");
    }

    @Override
    public boolean isLocal() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isLocal'");
    }

    @Override
    public String getGroup() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGroup'");
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


    
}
