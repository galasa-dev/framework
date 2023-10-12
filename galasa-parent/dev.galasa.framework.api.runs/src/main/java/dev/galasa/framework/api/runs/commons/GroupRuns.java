/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.commons;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IRun;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class GroupRuns extends BaseRoute {

    protected IFramework framework;
    

    public GroupRuns(ResponseBuilder responseBuilder, String path, IFramework framework) {
        super(responseBuilder, path);
        this.framework = framework;
    }

    protected List<IRun> getRuns(String groupName) throws InternalServletException {
         List<IRun> runs = null;
        try {
            runs = this.framework.getFrameworkRuns().getAllGroupedRuns(groupName);
        } catch (FrameworkException fe) {
            ServletError error = new ServletError(GAL5019_UNABLE_TO_RETRIEVE_RUNS, groupName);  
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }
        return runs;
    }

    protected ScheduleStatus serializeRuns(@NotNull List<IRun> runs) {
       
        ScheduleStatus status = new ScheduleStatus();
        boolean complete = true;
        for (IRun run : runs) {
            if (!"FINISHED".equalsIgnoreCase(run.getStatus()) &&
                    !"UP".equalsIgnoreCase(run.getStatus()) &&
                    !"DISCARDED".equalsIgnoreCase(run.getStatus())) {
                complete = false;
            }

            status.getRuns().add(run.getSerializedRun());
        }

        status.setComplete(complete);
        return status;
    }
}
