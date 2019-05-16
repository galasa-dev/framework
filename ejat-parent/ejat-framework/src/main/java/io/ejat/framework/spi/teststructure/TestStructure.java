package io.ejat.framework.spi.teststructure;

import java.util.List;

/**
 * <p>
 * This class represents the Test Class structure, ie it's test methods,
 * order of execution and their status
 *
 * @author Michael Baylis
 *
 */
public class TestStructure {

	private String bundle;
	private String testName;
	private String testShortName;
	
	private String status;
	
	private List<TestMethod> methods;

	
	
	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getTestShortName() {
		return testShortName;
	}

	public void setTestShortName(String testShortName) {
		this.testShortName = testShortName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<TestMethod> getMethods() {
		return methods;
	}

	public void setMethods(List<TestMethod> methods) {
		this.methods = methods;
	}



	public String report(String prefix) {
		String actualStatus = this.status;
		if (actualStatus == null) {
			actualStatus = "Unknown";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append("Test Class ");
		sb.append(this.testName);
		sb.append(" status=");
		sb.append(actualStatus);
		
		String methodPrefix = prefix + "    ";
		for(TestMethod method : this.methods) {
			method.report(methodPrefix, sb);
		}
		
		return sb.toString();
	}
}
