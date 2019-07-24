package dev.voras.framework.api.scheduleTests.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace="http://jaxb.jatp.cics.ibm.com")
@XmlType(namespace="http://jaxb.jatp.cics.ibm.com")
public class TestClass implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlElement(required=true)
	public UUID uuid;

	@XmlElement(required=true)
	public String name;
	
	@XmlElement(required=true)
	public ProgressStatus status;

	@XmlElement(required=false)
	public List<TestMethod> method;
	
	

	public void normalise() {
		if (name == null)        name = "";
		if (uuid == null)        uuid = UUID.fromString("000000000000000000000000000000");
		if (status == null)      status = ProgressStatus.UNKNOWN;
		if (method == null)      method = new ArrayList<TestMethod>(0);
		
		return;
	}
	
}
