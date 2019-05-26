package io.ejat.framework.spi;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

public interface IFrameworkRuns {
	
	@NotNull
	List<IRun> getActiveRuns() throws FrameworkException;

	@NotNull
	List<IRun> getQueuedRuns() throws FrameworkException;;
	
	@NotNull
	List<IRun> getAllRuns() throws FrameworkException;
	
	@NotNull
	Set<String> getActiveRunNames() throws FrameworkException;

	@NotNull
	IRun submitRun(String type,
			String requestor,
			String bundleName,
			String testName,
			String mavenRepository,
			String obr,
			String stream,
			boolean local) throws FrameworkException;

	boolean delete(String runname) throws DynamicStatusStoreException;

	IRun getRun(String runname) throws DynamicStatusStoreException;

	boolean reset(String runname) throws DynamicStatusStoreException;

}
