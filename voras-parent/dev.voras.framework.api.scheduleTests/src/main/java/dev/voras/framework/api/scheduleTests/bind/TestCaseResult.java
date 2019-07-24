package dev.voras.framework.api.scheduleTests.bind;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="testcaseresult", namespace="http://phoenix.devops.ibm.com")
@XmlAccessorType( XmlAccessType.FIELD )
public class TestCaseResult {
	
	@XmlElement(name="status", namespace="http://phoenix.devops.ibm.com")
	public String status;

	@XmlElement(name="runid", namespace="http://phoenix.devops.ibm.com")
	public String runId;
	@XmlElement(name="runidfiendly", namespace="http://phoenix.devops.ibm.com")
	public String runIdFriendly;

	@XmlElement(name="resulturi", namespace="http://phoenix.devops.ibm.com")
	public URI                    resultUri;
	@XmlElement(name="resulturidescription", namespace="http://phoenix.devops.ibm.com")
	public String                 resultUriDescription;
	
	@XmlElement(name="failuredocumentation", namespace="http://phoenix.devops.ibm.com")
	public String                 failureDocumentation;
	
	public String getStatus() {
		if (status == null) {
			return "None";
		}
		
		return status;
	}
	
	public String getRunName() {
		if (runIdFriendly == null) {
			return "None";
		}
		
		return runIdFriendly;
	}
	
	public boolean isPassed() {
		if ("PASSED".equals(status)) {
			return true;
		} else if ("PASSED_DEFECTS".equals(status)) {
			return true;
		} else if ("FINISHED_RUN".equals(status)) {
			return true;
		} else if ("FINISHED_DEFECTS_RUN".equals(status)) {
			return true;
		} else if ("IGNORED".equals(status)) {
			return true;
		} else if ("IGNORED_RUN".equals(status)) {
			return true;
		}
		
		return false;
	}

	public URI getResultUri() {
		return resultUri;
	}

	public String getResultUriDescription() {
		return resultUriDescription;
	}

	
}
