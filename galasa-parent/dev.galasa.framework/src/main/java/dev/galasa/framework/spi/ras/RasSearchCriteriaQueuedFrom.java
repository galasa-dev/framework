package dev.galasa.framework.spi.ras;
import java.time.Instant;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaQueuedFrom implements IRasSearchCriteria {
	
	private final Instant from;
	
	public RasSearchCriteriaQueuedFrom(Instant fromCriteria) {
		this.from = fromCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		if(from.equals(structure.getStartTime())) {
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}
	
}

