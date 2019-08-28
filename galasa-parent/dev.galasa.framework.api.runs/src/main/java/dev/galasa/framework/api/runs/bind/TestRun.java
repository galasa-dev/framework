package dev.galasa.framework.api.runs.bind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="testrun", namespace="http://phoenix.devops.ibm.com")
@XmlAccessorType( XmlAccessType.FIELD )
public class TestRun {
	
	@XmlAttribute(name="instance", namespace="http://phoenix.devops.ibm.com")
	public String instance;
	
	@XmlAttribute(name="type", namespace="http://phoenix.devops.ibm.com")
	public String type;
	
	@XmlElement(name="testcase", namespace="http://phoenix.devops.ibm.com")
	protected List<TestCase> testCases;
	
	@XmlElement(name="status", namespace="http://phoenix.devops.ibm.com")
	public Status status;
	
	@XmlElement(name="overrides", namespace="http://phoenix.devops.ibm.com")
	public String overrides;
	
	
	public TestRun() {
		this.testCases = new ArrayList<TestCase>();
		
		return;
	}
	
	@Override
	public int hashCode() {
		return this.instance.hashCode();
	}

	public String getInstance() {
		return this.instance;
	}
	
	public List<TestCase> getTestCases() {
		if (this.testCases == null) {
			this.testCases = new ArrayList<TestCase>();
		}
		
		return this.testCases;
	}

	public static TestRun getTestRun(String instance) {
		
		TestRun testRun = new TestRun();
		testRun.instance   = instance.toLowerCase();
		testRun.type       = "generic";
		testRun.overrides  = "";
		testRun.testCases  = new ArrayList<TestCase>();
		testRun.status     = Status.NOTRUN;
		
		return testRun;
	}

	public TestRun cloneWithNoCases() {
		TestRun clone = new TestRun();
		
		clone.instance  = this.instance;
		clone.overrides = this.overrides;
		clone.status    = Status.NOTRUN;
		clone.type      = this.type;
		clone.testCases = new ArrayList<TestCase>();
		
		return clone;
	}

	public Status getStatus() {
		return status;
	}

}
