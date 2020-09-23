package dev.galasa.framework.spi.ras;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaQueuedFrom implements IRasSearchCriteria {
	
	@Override
	public boolean criteriaMatched(TestStructure structure) {
		return Boolean.TRUE;
	}
	
}

