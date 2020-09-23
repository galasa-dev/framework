package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public interface IRasSearchCriteria {
	
	boolean criteriaMatched(@NotNull TestStructure testStructure);
	
}
