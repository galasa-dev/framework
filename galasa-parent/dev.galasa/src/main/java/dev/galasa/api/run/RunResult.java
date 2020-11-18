package dev.galasa.api.run;

import java.time.Instant;


public class RunResult {
	
    private String runId;
	private String runName;
	private String testName;
	private String testShortName;
	private String bundle;
	private String requestor;
	private String result;
	private String status;
	private Instant queued;
	private Instant start;
	private Instant end;
	
	public RunResult(String runId, String runName, String testName, String testShortName, String bundle,
			String requestor, String result, String status, Instant queued, Instant start, Instant end) {
		
	    this.runId = runId;
		this.runName = runName;
		this.testName = testName;
		this.testShortName = testShortName;
		this.bundle = bundle;
		this.requestor = requestor;
		this.result = result;
		this.status = status;
		this.queued = queued;
		this.start = start;
		this.end = end;
		
	}
	
	
	public void setRunName(String runName) {
		this.runName = runName;
	}
	
	public void setTestName(String testName) {
		this.testName = testName;
	}
	
	public void setTestShortName(String testShortName) {
		this.testShortName = testShortName;
	}
	
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
	
	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setQueued(Instant queued) {
		this.queued = queued;
	}
	
	public void setStart(Instant start) {
		this.start = start;
	}
	
	public void setEnd(Instant end) {
		this.end = end;
	}
	
	public String getRunId() {
	   return this.runId;
	}
	
	public String getRunName() {
		return this.runName;
	}
	
	public String getTestName() {
		return this.testName;
	}
	
	public String getTestShortName() {
		return this.testShortName;
	}
	
	public String getBundle() {
		return this.bundle;
	}
	
	public String getRequestor() {
		return this.requestor;
	}
	
	public String getResult() {
		return this.result;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public Instant getQueued() {
		return this.queued;
	}
	
	public Instant getStart() {
		return this.start;
	}
	
	public Instant getEnd() {
		return this.end;
	}

}
