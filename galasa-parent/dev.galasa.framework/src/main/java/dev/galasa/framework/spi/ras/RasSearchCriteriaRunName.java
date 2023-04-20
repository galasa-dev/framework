package dev.galasa.framework.spi.ras;

import java.util.Set;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaRunName implements IRasSearchCriteria {
	
private final Set<String> runNames;
	
	public RasSearchCriteriaRunName(@NotNull Set<String> testRunNames) {
		this.runNames = testRunNames;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		boolean isMatched = false;
		if(structure != null) {
			if(runNames != null) {
				isMatched = runNames.contains(structure.getRunName());
			}
		}
		return isMatched;
	}
	
}
	
