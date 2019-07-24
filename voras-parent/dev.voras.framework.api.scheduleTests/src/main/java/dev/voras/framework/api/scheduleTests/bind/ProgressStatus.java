package dev.voras.framework.api.scheduleTests.bind;

import javax.xml.bind.annotation.XmlType;



/**
 * Indicates the status of a test case or run
 * 
 * @author Michael Baylis
 *
 */
@XmlType(namespace="http://jaxb.jatp.cics.ibm.com")
public enum ProgressStatus {

	/**
	 * The test is currently running
	 */
	RUNNING(true, false),
	/**
	 * The test has passed
	 */
	PASSED(false, true),
	/**
	 * The test has passed
	 */
	PASSED_DEFECTS(false, true),
	/**
	 * The test has failed
	 */
	FAILED(false, true),
	/**
	 * The test has failed
	 */
	FAILED_DEFECTS(false, true),
	/**
	 * Supporting method completed ok
	 */
	OK(true, false),
	/**
	 * The test was aborted
	 */
	ABORTED(false, true),
	/**
	 * The test failed to build the environment
	 */
	ENVIRONMENT_FAILED(false, true),
	/**
	 * The test failed due to a problem with the infrastructure
	 */
	INFRASTRUCTURE_FAILED(false, true),
	/**
	 * The test has been ignored
	 */
	IGNORED(false, true),
	/**
	 * The test has finished
	 */
	FINISHED(false, true),
	/**
	 * The test has started
	 */
	STARTED(true, false),
	/**
	 * Ready
	 */
	READY(false, false),
	/**
	 * Preallocation Failed
	 */
	PREALLOCATION_FAILED(false, false),
	
	UNKNOWN(false, false);

	private final boolean active;
	private final boolean complete;
	
	ProgressStatus(boolean active, boolean complete) {
		this.active = active;
		this.complete = complete;
	}
	
	public boolean isActive() {
		return active;
	}

	public boolean isComplete() {
		return complete;
	}
}
