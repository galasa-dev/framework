package io.ejat.framework.spi.teststructure;

import java.util.List;

public class TestMethod {
	
	private String methodName;
	private String type;
	
	private List<TestMethod> befores;
	private List<TestMethod> afters;
	
	private String status;
	
	private String exception;
	
	private long   runLogStart;
	private long   runLogEnd;

	
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<TestMethod> getBefores() {
		return befores;
	}

	public void setBefores(List<TestMethod> befores) {
		this.befores = befores;
	}

	public List<TestMethod> getAfters() {
		return afters;
	}

	public void setAfters(List<TestMethod> afters) {
		this.afters = afters;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public long getRunLogStart() {
		return runLogStart;
	}

	public void setRunLogStart(long runLogStart) {
		this.runLogStart = runLogStart;
	}

	public long getRunLogEnd() {
		return runLogEnd;
	}

	public void setRunLogEnd(long runLogEnd) {
		this.runLogEnd = runLogEnd;
	}


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
