package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaTestName implements IRasSearchCriteria {
	
	private final String[] testNames;
	
	public RasSearchCriteriaTestName(String... testNameCriteria) {
		this.testNames = testNameCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		for(String testName : testNames) {
			if(testName.equals(structure.getTestName())) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}
}
