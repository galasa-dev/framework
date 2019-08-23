package dev.galasa.framework.api.scheduleTests.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.SerializedRun;

public class ScheduleStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public RunStatus scheduleStatus;
	
	public List<SerializedRun> runs;
	
	public ScheduleStatus() {
		runs = new ArrayList<>();
	}
}
