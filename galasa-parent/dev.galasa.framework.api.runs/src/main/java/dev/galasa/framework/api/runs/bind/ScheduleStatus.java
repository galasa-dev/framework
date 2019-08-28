package dev.galasa.framework.api.runs.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.SerializedRun;

public class ScheduleStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private RunStatus scheduleStatus;
	
	private List<SerializedRun> runs;
	
	public ScheduleStatus() {
		runs = new ArrayList<>();
	}

	public RunStatus getScheduleStatus() {
		return scheduleStatus;
	}

	public void setScheduleStatus(RunStatus scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}

	public List<SerializedRun> getRuns() {
		return runs;
	}

	public void setRuns(List<SerializedRun> runs) {
		this.runs = runs;
	}
}
