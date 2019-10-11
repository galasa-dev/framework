package dev.galasa.framework.api.runs.bind;

import dev.galasa.framework.SerializedRun;

public class TestCase {
	
	private String bundleName;
	private String className;
	private String type;
	private String stream;
	private SerializedRun runDetails;
	
	public String getBundleName() {
		return this.bundleName;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public String getFullName() {
		return bundleName + "/" + className;
	}
	
	public TestCase getClone() {
		TestCase newCase = new TestCase();
		
		newCase.bundleName = this.bundleName;
		newCase.className = this.className;
		newCase.type = this.type;
		newCase.stream = this.stream;
		newCase.runDetails = this.runDetails;
		
		return newCase;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getStream() {
		return stream;
	}


	public void setStream(String stream) {
		this.stream = stream;
	}


	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}


	public void setClassName(String className) {
		this.className = className;
	}

	public SerializedRun getRunDetails() {
		return runDetails;
	}

	public void setRunDetails(SerializedRun runDetails) {
		this.runDetails = runDetails;
	}


}
