package dev.galasa.framework.api.runs.bind;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

public class TestCaseResult {
	
	private String status;
	private String runId;
	private String runIdFriendly;
	private URI    resultUri;
	private String resultUriDescription;
	private String failureDocumentation;
	
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
