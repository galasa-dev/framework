/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.runs.bind;

public enum Status {
	NOTRUN(true, true),
	STARTED(true, false),
	FAILED(false, false),
	SUCCESS(true, true),
	BYPASSED(false, false);
	
	
	private final boolean runNormalStep;
	private final boolean completed;
	
	private Status(boolean runNormalStep, boolean completed) {
		this.runNormalStep = runNormalStep;
		this.completed     = completed;
	}
	
	public boolean okToRunNormalStep() {
		return runNormalStep;
	}
	
	public boolean isCompleted() {
		return completed;
	}
}
