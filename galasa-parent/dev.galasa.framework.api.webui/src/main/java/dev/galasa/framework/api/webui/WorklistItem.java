package dev.galasa.framework.api.webui;

public class WorklistItem {
	
	private String runId;
	private String runName;
	private String shortName;
	private String testClass;
	private String result;
	   
	   public WorklistItem(String runId, String runName, String shortName, String testClass, String result) {
	      this.runId = runId;
	      this.runName = runName;
	      this.shortName = shortName;
	      this.testClass = testClass;
	      this.result = result;
	   }

	   public String getRunId() {
	      return runId;
	   }

	   public void setRunId(String runId) {
	      this.runId = runId;
	   }

	   public String getRunName() {
		   return runName;
	   }
	   
	   public void setRunName(String runName) {
		   this.runName = runName;
	   }
	   
	   public String getShortName() {
		   return shortName;
	   }
	   
	   public void setShortName(String shortName) {
		   this.shortName = shortName;
	   }
	   
	   public String getTestClass() {
		   return testClass;
	   }
	   
	   public void setTestClass(String testClass) {
		   this.testClass = testClass;
	   }
	   
	   public String getResult() {
		   return result;
	   }
	   
	   public void setResult(String result) {
		   this.result = result;
	   }

}
