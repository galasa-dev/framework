/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.runs.bind;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class ScheduleRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<String>  classNames;
	private List<String>  rerunUUIDs;
	private List<String>  testingAreas;
	private RequestorType requestorType = RequestorType.INDIVIDUAL;
	private String        buildVersion;
	private String        testStream;
	private String		  obr;
	private String        mavenRepository;
	private boolean		  trace;
	
	private List<JatJaxbProperties> runProperties;


	public static class JatJaxbProperties implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private String key;
		private String value;
		
		@Override
		public String toString() {
			return key + "=" + value;
		}
	}

	public Properties getRunProperties() {
		return convertToProperties(runProperties);
	}

	
	public void setRunProperties(Properties runProperties) {
		this.runProperties = convertFromProperties(runProperties);
	}
	

	private List<JatJaxbProperties> convertFromProperties(Properties properties) {
		if (properties == null) {
			return null;
		}
		
		List<JatJaxbProperties> jatProps = new ArrayList<JatJaxbProperties>();
		for(Entry<Object, Object> entry : properties.entrySet()) {
			JatJaxbProperties jatProp = new JatJaxbProperties();
			jatProp.key   = entry.getKey().toString();
			jatProp.value = entry.getValue().toString();
			jatProps.add(jatProp);
		}
		
		return jatProps;
	}
	
	private Properties convertToProperties(List<JatJaxbProperties> jaxbProperties) {
		if (jaxbProperties == null) {
			return null;
		}
		
		Properties props = new Properties();
		
		for(JatJaxbProperties jatProp : jaxbProperties) {
			props.put(jatProp.key, jatProp.value);
		}
		
		return props;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public List<String> getClassNames() {
		return classNames;
	}


	public List<String> getRerunUUIDs() {
		return rerunUUIDs;
	}


	public List<String> getTestingAreas() {
		return testingAreas;
	}


	public RequestorType getRequestorType() {
		return requestorType;
	}


	public String getBuildVersion() {
		return buildVersion;
	}


	public String getTestStream() {
		return testStream;
	}


	public String getObr() {
		return obr;
	}


	public String getMavenRepository() {
		return mavenRepository;
	}


	public void setClassNames(List<String> classNames) {
		this.classNames = classNames;
	}


	public void setRerunUUIDs(List<String> rerunUUIDs) {
		this.rerunUUIDs = rerunUUIDs;
	}


	public void setTestingAreas(List<String> testingAreas) {
		this.testingAreas = testingAreas;
	}


	public void setRequestorType(RequestorType requestorType) {
		this.requestorType = requestorType;
	}


	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}


	public void setTestStream(String testStream) {
		this.testStream = testStream;
	}


	public void setObr(String obr) {
		this.obr = obr;
	}


	public void setMavenRepository(String mavenRepository) {
		this.mavenRepository = mavenRepository;
	}


	public void setRunProperties(List<JatJaxbProperties> runProperties) {
		this.runProperties = runProperties;
	}


	public boolean isTrace() {
		return trace;
	}


	public void setTrace(boolean trace) {
		this.trace = trace;
	}	
	

}
