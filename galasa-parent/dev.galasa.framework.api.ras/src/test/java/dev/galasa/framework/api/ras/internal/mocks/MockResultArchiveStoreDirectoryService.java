/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.mocks;

import java.util.*;
import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasTestClass;

public class MockResultArchiveStoreDirectoryService implements IResultArchiveStoreDirectoryService {

    private List<IRunResult> getRunsResults ;

    public MockResultArchiveStoreDirectoryService(List<IRunResult> getRunsResults) {
        this.getRunsResults = getRunsResults ;
    }

	@Override
	public @NotNull String getName() {
		throw new UnsupportedOperationException("Unimplemented method 'getName'");
	}

	@Override
	public boolean isLocal() {
		throw new UnsupportedOperationException("Unimplemented method 'isLocal'");
	}


	@Override
	public @NotNull List<IRunResult> getRuns(@NotNull IRasSearchCriteria... searchCriteria)
			throws ResultArchiveStoreException {
		return this.getRunsResults;
	}

	@Override
	public @NotNull List<String> getRequestors() throws ResultArchiveStoreException {
		throw new UnsupportedOperationException("Unimplemented method 'getRequestors'");
	}

	@Override
	public @NotNull List<RasTestClass> getTests() throws ResultArchiveStoreException {
		throw new UnsupportedOperationException("Unimplemented method 'getTests'");
	}

	@Override
	public @NotNull List<String> getResultNames() throws ResultArchiveStoreException {
		throw new UnsupportedOperationException("Unimplemented method 'getResultNames'");
	}

	@Override
	public IRunResult getRunById(@NotNull String runId) throws ResultArchiveStoreException {
		List <IRunResult> runResults = this.getRunsResults;
		if (runResults != null) {
			for (int c =0; c < runResults.size(); c++){
				IRunResult match = runResults.get(c);
				if ( match.getRunId().equals(runId)){
					return  match;
				}
			}	
		} else {
			return null;
		}
		throw new ResultArchiveStoreException("Run id not found in mock getRunById().");
	}
    
}