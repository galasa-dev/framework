package dev.galasa.framework.api.scheduleTests.bind;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class ScheduleRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public List<String>  classNames;
	public List<String>  rerunUUIDs;
	public List<String>  testingAreas;
	public RequestorType requestorType = RequestorType.INDIVIDUAL;
	public String        buildVersion;
	public String        testStream;
	public String		 obr;
	public String        mavenRepository;
	
	private List<JatJaxbProperties> runProperties;


	public static class JatJaxbProperties implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public String key;
		public String value;
		
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
	

}
