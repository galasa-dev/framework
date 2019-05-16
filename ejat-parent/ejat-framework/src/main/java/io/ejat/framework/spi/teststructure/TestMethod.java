package io.ejat.framework.spi.teststructure;

import java.util.List;

public class TestMethod {
	
	public String methodName;
	public String type;
	
	public List<TestMethod> befores;
	public List<TestMethod> afters;
	
	public String status;
	
	public String exception;
	
	public long   runLogStart;
	public long   runLogEnd;
	
	public void report(String prefix, StringBuilder sb) {
		String actualStatus = this.status;
		if (actualStatus == null) {
			actualStatus = "Unknown";
		}
		
		String subPrefix = prefix + "    ";
		for(TestMethod before : this.befores) {
			before.report(subPrefix, sb);
		}
		
		sb.append(prefix);
		sb.append("Test Method ");
		sb.append(methodName);
		sb.append(", type=");
		sb.append(type);
		sb.append(", status=");
		sb.append(actualStatus);
		
		
		for(TestMethod after : this.afters) {
			after.report(subPrefix, sb);
		}
		
		return;
	}

}
