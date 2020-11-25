package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaRequestor implements IRasSearchCriteria{
	
	private final String[] requestors;
	
	public RasSearchCriteriaRequestor(@NotNull String... requestorCriteria) {
		this.requestors = requestorCriteria;
	}
	
	@Override
	public boolean criteriaMatched(@NotNull TestStructure structure) {
		
		if(structure == null) {
			return Boolean.FALSE;	
		}
		
		if(requestors != null) {
			for(String requestor : requestors) {
				if(requestor.equals(structure.getRequestor())){
					return Boolean.TRUE;
				}
			}
		}
		
		return Boolean.FALSE;
	}

    public String[] getRequestors() {
        return requestors;
    }
}
