package dev.galasa.framework.api.scheduleTests.bind;

public class TestSelection {
	
	public String instance;
	public String type;
	public Status status;
	public String overrides;
	
	public TestSelection() {
		return;
	}
	
	@Override
	public int hashCode() {
		return this.instance.hashCode();
	}

	public String getInstance() {
		return this.instance;
	}
	
	public static TestSelection getTestSelection(String instance) {
		
		TestSelection testSelection = new TestSelection();
		testSelection.instance   = instance.toLowerCase();
		testSelection.type       = "generic";
		testSelection.overrides  = "";
		testSelection.status     = Status.NOTRUN;
		
		return testSelection;
	}

	public String getType() {
		return type;
	}

	public Status getStatus() {
		return status;
	}

	public String getOverrides() {
		return overrides;
	}

}
