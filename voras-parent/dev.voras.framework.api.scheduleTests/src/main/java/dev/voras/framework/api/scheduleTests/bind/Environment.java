package dev.voras.framework.api.scheduleTests.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace="http://jaxb.jatp.cics.ibm.com")
public class Environment implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlElement(required=true)
	public UUID uuid;

	@XmlElement(required=true)
	public String name;
	
	@XmlElement(required=false)
	public boolean conrepPresent;
	
	@XmlElement(name="class",required=false)
	public List<TestClass> klass;
	
	@XmlElement(required=false)
	public List<JobOutput> buildJob;
	
	@XmlElement(required=false)
	public List<JobOutput> region;
	
	@XmlElement(required=false)
	public List<JobOutput> discardJob;
	

	public void normalise() {
		if (name == null)        name = "";
		if (uuid == null)        uuid = UUID.fromString("000000000000000000000000000000");
		if (klass == null)       klass = new ArrayList<TestClass>(0);
		if (buildJob == null)    buildJob = new ArrayList<JobOutput>(0);
		if (region == null)      region = new ArrayList<JobOutput>(0);
		if (discardJob == null)  discardJob = new ArrayList<JobOutput>(0);
		
		return;
	}

	
}
