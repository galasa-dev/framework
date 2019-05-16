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

	public String bundle;
	public String testName;
	public String testShortName;
	
	public String status;
	
	public List<TestMethod> methods;

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
