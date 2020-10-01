package dev.galasa.api.run;

import java.time.Instant;

import javax.validation.constraints.NotNull;

public class RunResult {
	
	private final String runName;
	private final String testName;
	private final String testShortName;
	private final String bundle;
	private final String requestor;
	private final String result;
	private final String status;
	private final Instant queued;
	private final Instant start;
	private final Instant end;
	
	public RunResult(String runName, String testName, String testShortName, String bundle,
			String requestor, String result, String status, Instant queued, Instant start, Instant end) {
		
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
