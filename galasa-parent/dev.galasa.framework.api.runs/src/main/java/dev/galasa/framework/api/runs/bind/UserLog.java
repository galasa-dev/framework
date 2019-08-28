package dev.galasa.framework.api.runs.bind;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace="http://jaxb.jatp.cics.ibm.com")
@XmlType(namespace="http://jaxb.jatp.cics.ibm.com")
public class UserLog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlElement(required=true)
	public String name;
	@XmlElement(required=true)
	public UUID uuid;
	
}
