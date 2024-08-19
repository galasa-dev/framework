/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.util.List;

import dev.galasa.framework.spi.IRunResult;

/**
 * An internal bean class representing a page of runs stored in the RAS,
 * which includes a token pointing to the next page of runs for 
 * cursor-based pagination
 */
public class RasRunResultPage {
    
    private List<IRunResult> runs;
    private String nextPageToken;

    public RasRunResultPage(List<IRunResult> runs, String nextPageToken) {
        this.runs = runs;
        this.nextPageToken = nextPageToken;
    }

    public List<IRunResult> getRuns() {
        return runs;
    }

    public void setRuns(List<IRunResult> runs) {
        this.runs = runs;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
