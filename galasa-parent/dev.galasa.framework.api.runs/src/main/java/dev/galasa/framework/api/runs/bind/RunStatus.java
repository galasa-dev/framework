/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.runs.bind;

import javax.xml.bind.annotation.XmlType;




/**
 * Indicates the status of a test case or run
 * 
 * @author Michael Baylis
 *
 */
@XmlType(namespace="http://jaxb.jatp.cics.ibm.com")
public enum RunStatus {

	/**
	 * The test is currently running
	 */
	SUBMITTED("Submitted", false),
	WAITING("Waiting", false),
	ALLOCATED("Allocated", false),
	STARTED_RUN("Run Started", false),
	FINISHED_RUN("Run Finished - PASS", true),  
	FINISHED_DEFECTS_RUN("Run Finished - PASS WITH DEFECTS", true),  
	ABORTED("Aborted", true),
	FAILED_RUN("Run Finished - FAIL", true),
	FAILED_DEFECTS_RUN("Run Finished - FAIL WITH DEFECTS", true),
	IGNORED_RUN("Run Finished - IGNORED", true),
	TESTING("Running Tests", false),
	PRE_ALLOCATING("Preallocating", false),
	BUILDING_ENVIRONMENT("Building Environment", false),
	GENERATE_ARTIFACTS("Generating Artifacts", false),
	STARTING_ENVIRONMENT("Starting Environment", false),
	STOPPING_ENVIRONMENT("Shutting Down Environment", false),
	CLEARING_ARTIFACTS("Clearing Artifacts", false),
	DISCARDING_ENVIRONMENT("Discarding Environment", false),
	UNKNOWN("Unknown",false);
	
	
	
	private final String label;
	private final boolean isRunComplete;
	
	RunStatus(String label, boolean isRunComplete) {
		
		this.label         = label;
		this.isRunComplete = isRunComplete;
		
		return;
	}
	
	public String getLabel() {
		return label;
	}
	
	public boolean isRunComplete() {
		return isRunComplete;
	}

}
