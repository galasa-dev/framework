package dev.voras.framework.api.scheduleTests.bind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.voras.framework.RunImpl;
import dev.voras.framework.SerializedRun;
import dev.voras.framework.spi.IRun;

public class ScheduleStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public RunStatus scheduleStatus;
	
	public List<SerializedRun> runs;
	
	public ScheduleStatus() {
		runs = new ArrayList<>();
	}
}
