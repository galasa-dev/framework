package dev.galasa.framework.api.scheduleTests.bind;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace="http://jaxb.jatp.cics.ibm.com")
public class JobOutput implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlElement
	public String jobname;
	@XmlElement
	public String jobid;
	@XmlElement
	public UUID   logUUID;
	
	public void normalise() {
		if (jobname == null)        jobname = "";
		if (jobid == null)          jobid = "";
		if (logUUID == null)        logUUID = UUID.fromString("000000000000000000000000000000");
		
		return;
	}

}
