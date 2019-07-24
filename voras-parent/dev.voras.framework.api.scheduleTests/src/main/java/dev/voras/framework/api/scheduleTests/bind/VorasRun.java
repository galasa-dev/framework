package dev.voras.framework.api.scheduleTests.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * XML representation of a test run
 * 
 * @author root
 *
 */
@XmlRootElement(namespace="http://jaxb.jatp.cics.ibm.com")
@XmlType(namespace="http://jaxb.jatp.cics.ibm.com")
public class VorasRun implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlElement(required=true)
	public String name;
	
	@XmlElement(required=true)
	public UUID uuid;
	
	@XmlElement(required=false)
	public String requestor;
	
	@XmlElement(required=false) 
	public String requested;
	
	@XmlElement(required=false)
	public RunStatus status;
	
	@XmlElement(required=false) 
	public String waiting;
	
	@XmlElement(required=false)
	public String engine;
	
	@XmlElement(required=false)
	public String runParameters;
	
	@XmlElement(required=false)
	public String frameworkXML;
	
	@XmlElement(required=false)
	public String notificationList;
	
	@XmlElement(required=false)
	public String testProperties;
	
	@XmlElement(required=false)
	public Boolean runLogPresent;
	
	@XmlElement(required=false)
	public int     runLogCount;
	
	@XmlElement(required=false)
	public Boolean terminalsPresent;
	
	@XmlElement(required=false)
	public Boolean testsPresent;
	
	@XmlElement(required=false)
	public Boolean aborting;
	
	@XmlElement(required=false)
	public UUID schedule;
	
	@XmlElement(required=false)
	public Boolean failureAnalysisPresent;
	
	@XmlElement(required=false)
	public List<Environment> environment;
	
	@XmlElement(required=false)
	public List<UserLog> userLogs;
	
	@XmlElement(required=false)
	public int totalMethods;
	
	@XmlElement(required=false)
	public int completedMethods;
	
	@XmlElement(required=false)
	public int failedMethods;
	
	public void normalise() {
		if (name == null)           name = "";
		if (uuid == null)           uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
		if (requestor == null)      requestor = "";
		if (requested == null)      requested = "2012/01/01 00:00:00 +0000";
		if (status == null)         status = RunStatus.UNKNOWN;
		if (environment == null)    environment = new ArrayList<Environment>(0);
		if (userLogs == null)       userLogs = new ArrayList<UserLog>(0);
		if (engine == null)         engine = "";
		if (runParameters == null)  runParameters = "";
		if (testProperties == null) testProperties = "";
		if (notificationList == null) notificationList = "";
		
		return;
	}
	
	
	public TestMethod locateTestMethod(UUID uuid) {
		if(environment != null) {
			for(Environment env : environment) {
				if(env.klass != null){
					for(TestClass cls : env.klass) {
						if(cls.method != null) {
							for(TestMethod met : cls.method) {
								if(met.uuid.equals(uuid)){
									return met;
								}
							}
						}
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Search for an environment definition by UUID within this run
	 * @param uuid
	 * @return
	 */
	protected Environment locateEnvironment(UUID uuid) {
		if(environment != null) {
			for(Environment env : environment) {
				if(env.uuid.equals(uuid)){
					return env;
				}
			}
		}

		return null;
	}
	
	/**
	 * Search for a test class by UUID within this run
	 * 
	 * @param uuid
	 * @return
	 */
	public TestClass locateTestClass(UUID uuid) {
		if(environment != null) {
			for(Environment env : environment) {
				if(env.klass != null){
					for(TestClass cls : env.klass) {
						if(cls.uuid.equals(uuid)){
							return cls;
						}
					}
				}
			}
		}

		return null;
	}
	
	public List<TestClass> listTestClasses() {
		LinkedList<TestClass> results = new LinkedList<TestClass>();
		
		if(environment != null) {
			for(Environment env : environment) {
				if(env.klass != null){
					results.addAll(env.klass);
				}
			}
		}
		
		return results;
	}
	
	
	public List<TestMethod> listTestMethods() {
		LinkedList<TestMethod> results = new LinkedList<TestMethod>();
		
		for(TestClass kls : listTestClasses()) {
			if(kls.method != null) {
				results.addAll(kls.method);
			}
		}
		
		return results;
	}
}
