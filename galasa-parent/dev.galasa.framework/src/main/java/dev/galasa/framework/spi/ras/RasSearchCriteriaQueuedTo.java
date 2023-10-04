/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaQueuedTo implements IRasSearchCriteria {
	
	private final Instant to;
	
	public RasSearchCriteriaQueuedTo(@NotNull Instant toCriteria) {
		this.to = toCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		if(structure == null) {
			return false;	
		}
		
		if(structure.getEndTime() == null) {
			return false;
		}
		
		if(to == null) {
			return false;
		}
		
		if(to.equals(structure.getEndTime()) || to.isAfter(structure.getEndTime())) {
			return true;
		}
		
		return false;
	}

    public Instant getTo() {
        return this.to;
    }
}
