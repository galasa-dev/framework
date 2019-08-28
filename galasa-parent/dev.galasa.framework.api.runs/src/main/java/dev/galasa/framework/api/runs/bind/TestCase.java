package dev.galasa.framework.api.runs.bind;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

public class TestCase {
	
	private String bundleName;
	private String className;
	private String type;
	private String stream;
	protected List<TestCaseResult> results;
	
	public TestCase() {
		this.results = new ArrayList<TestCaseResult>();
		return;
	}
	
	
	public String getBundleName() {
		return this.bundleName;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public String getFullName() {
		return bundleName + "/" + className;
	}
	
	public List<TestCaseResult> getResults() {
		if (results == null) {
			results = new ArrayList<TestCaseResult>();
		}
		
		return results;
	}
	
	public TestCase getClone() {
		TestCase newCase = new TestCase();
		
		newCase.bundleName = this.bundleName;
		newCase.className = this.className;
		newCase.type = this.type;
		newCase.stream = this.stream;
		newCase.results = new ArrayList<TestCaseResult>();
		
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


	public void setResults(List<TestCaseResult> results) {
		this.results = results;
	}

}
