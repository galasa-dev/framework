package io.ejat.framework.spi;

import java.nio.file.FileSystem;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * <p>Used to gain access to the Result Archive Store (RAS)</p>
 * 
 * <p>POnce initialised, the Framework will provide access to 0 or more Result Archive Stores.</p>
 * 
 * <p>Apache CouchDB is the preferred RAS for an automation, and a file directory on the local filesystem
 * is the preferred RAS for local runs.</p>
 * 
 * <p>An {@link IResultArchiveStore} can be obtained from {@link IFramework#getResultArchiveStore(String)}.
 * </p> 
 * 
 * @author Michael Baylis
 *
 */
public interface IResultArchiveStore {
	
	/**
	 * The type of artifacts that can be stored in the Result Archive Store
	 * 
	 * @author Michael Baylis
	 *
	 */
	public enum ARTIFACT_TYPES {
		TEXT,
		BINARY,
		XML,
		JSON,
		PNG,
		TERMINAL
	}
	
	/**
	 * Write a message to the run log in the RASs
	 * 
	 * @param message - a Message to write to the run log
	 */
	void writeLog(@NotNull String message);
	
	/**
	 * Write multiple messages to the run log in the RASs
	 * 
	 * @param messages - Messages to write
	 */
	void writeLog(@NotNull List<String> messages);
	
	/**
	 * Update the Test Structure object in the RASs with the current status
	 * 
	 * @param testStructure
	 */
	void updateTestStructure(@NotNull ITestStructure testStructure);
	
	/**
	 * Obtain the Filesystem that represents the RASs for storing Stored Artifacts
	 * 
	 * @return a {@link java.nio.file.FileSystem}
	 */
	FileSystem getStoredArtifactsFileSystem();
	
}
