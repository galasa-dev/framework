package io.ejat.framework.spi;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

public interface IFrameworkRuns {
	
	@NotNull
	List<IRun> getActiveRuns() throws FrameworkException;
	
	@NotNull
	List<IRun> getAllRuns() throws FrameworkException;
	
	@NotNull
	Set<String> getActiveRunNames() throws FrameworkException;

}
