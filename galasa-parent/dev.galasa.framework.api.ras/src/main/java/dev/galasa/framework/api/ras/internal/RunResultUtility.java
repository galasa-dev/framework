package dev.galasa.framework.api.ras.internal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.api.ras.RasArtifact;
import dev.galasa.api.ras.RasRunResult;
import dev.galasa.api.ras.RasTestMethod;
import dev.galasa.api.ras.RasTestStructure;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestMethod;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class RunResultUtility {
	
	
	public RunResultUtility() {

	}
	
	public static @NotNull RasRunResult toRunResult(@NotNull IRunResult runResult, boolean isShort) throws ResultArchiveStoreException {
	   
	    String runId = runResult.getRunId();
		RasTestStructure structure = getTestStructure(runResult);
		Path artifactsPath = runResult.getArtifactsRoot();
		List<RasArtifact> artifacts = new ArrayList<>();
		
		if(isShort) {
      	   structure.setMethods(null);
      	   artifacts = null;
		}else {
		   artifacts = getArtifacts(artifactsPath);
		}
		
		RasRunResult newRunResult = new RasRunResult(runId, artifacts, structure);
		
		return newRunResult;
		
	}
	
	private static List<RasArtifact> getArtifacts(Path path){
	   
	   List<RasArtifact> artifacts = new ArrayList<>();
	   
	   return artifacts;
	   
	}
	
	private static RasTestStructure getTestStructure(IRunResult runResult) throws ResultArchiveStoreException {
	   TestStructure struc = runResult.getTestStructure();
	   List<TestMethod> methods = struc.getMethods();
	   
	   String runName = struc.getRunName();
	   String bundle = struc.getBundle();
	   String testName = struc.getTestName();
	   String testShortName = struc.getTestShortName();
	   String requestor = struc.getRequestor();
	   String status = struc.getStatus();
	   String result = struc.getResult();
	   Instant queued = struc.getQueued();
	   Instant startTime = struc.getStartTime();
	   Instant endTime = struc.getEndTime();
	   List<RasTestMethod> rasMethods = convertMethods(methods);
	   
	   return new RasTestStructure(runName, bundle, testName, testShortName, requestor, status, result, queued, startTime, endTime, rasMethods);
	}
	
	private static List<RasTestMethod> convertMethods(List<TestMethod> methods){
	   List<RasTestMethod> rasMethods = new ArrayList<>();
	   
	   if(methods != null) {
      	   for(TestMethod method : methods) {
      	      String className = method.getClassName();
      	      String methodName = method.getMethodName();
      	      String type = method.getType();
      	      String status = method.getStatus();
      	      String result = method.getResult();
      	      Instant startTime = method.getStartTime();
      	      Instant endTime = method.getEndTime();
      	      int runLogStart = method.getRunLogStart();
      	      int runLogEnd = method.getRunLogEnd();
      	      List<RasTestMethod> befores = convertMethods(method.getBefores());
      	      List<RasTestMethod> afters = convertMethods(method.getAfters());
      	      
      	      RasTestMethod rasMethod = new RasTestMethod(className, methodName, type, status, result, startTime, endTime, 
      	            runLogStart, runLogEnd, befores, afters);
      	      
      	      rasMethods.add(rasMethod);
      	   }
	   }
	   
	   return rasMethods;
	   
	}
	
}
