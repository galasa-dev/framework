package dev.galasa.framework.spi.ras;

import dev.galasa.framework.spi.teststructure.TestStructure;

public interface IRasSearchCriteria {
	
	boolean criteriaMatched(TestStructure testStructure);
	
}
