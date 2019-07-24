package dev.voras.framework.api.scheduleTests.bind;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

public class ScheduleStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public RunStatus scheduleStatus;
	
	public List<VorasRun> runs;
}
