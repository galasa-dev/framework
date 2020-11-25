package dev.galasa.framework.api.ras.internal;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import dev.galasa.api.run.RunResult;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public class RunResultUtility {
	
	
	public RunResultUtility() {

	}
	
	public static @NotNull RunResult toRunResult(@NotNull IRunResult runResult) throws ResultArchiveStoreException {
		
	    String runId = runResult.getRunId();
		String runName = runResult.getTestStructure().getRunName();
		String testName = runResult.getTestStructure().getTestName();
		String testShortName = runResult.getTestStructure().getTestShortName();
		String bundle = runResult.getTestStructure().getBundle();
		String requestor = runResult.getTestStructure().getRequestor();
		String result = runResult.getTestStructure().getResult();
		String status = runResult.getTestStructure().getStatus();
		Instant queued = runResult.getTestStructure().getQueued();
		Instant start = runResult.getTestStructure().getStartTime();
		Instant end = runResult.getTestStructure().getEndTime();
		
		RunResult newRunResult = new RunResult(runId, runName, testName, testShortName, bundle, requestor,
													result, status, queued, start, end);
		
		return newRunResult;
		
	}
	
}
