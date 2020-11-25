package dev.galasa.framework.api.ras.internal;
import dev.galasa.framework.spi.ras.Artifact;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.api.run.RunResult;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class RunResultUtility {
	
	
	public RunResultUtility() {

	}
	
	public static @NotNull RunResult toRunResult(@NotNull IRunResult runResult, boolean isShort) throws ResultArchiveStoreException {
	   
	    String runId = runResult.getRunId();
		TestStructure structure = runResult.getTestStructure();
		Path artifactsPath = runResult.getArtifactsRoot();
		List<Artifact> artifacts = new ArrayList<>();
		
		if(isShort) {
      	   structure.setArtifactRecordIds(null);
      	   structure.setGherkinMethods(null);
      	   structure.setLogRecordIds(null);
      	   structure.setMethods(null);
      	   artifacts = null;
		}else {
		   artifacts = getArtifacts(artifactsPath);
		}
		
		RunResult newRunResult = new RunResult(runId, structure, artifacts);
		
		return newRunResult;
		
	}
	
	private static List<Artifact> getArtifacts(Path path){
	   
	   List<Artifact> artifacts = new ArrayList<>();
	   
	   return artifacts;
	   
	}
	
}
