package io.ejat.framework.internal.ras;

import java.nio.file.Path;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IResultArchiveStore;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ResultArchiveStoreException;
import io.ejat.framework.spi.teststructure.ITestStructure;

/**
 * Stub for the Result Archive Store. It is intended that this code will be able
 * to drive 2+ RASs
 *
 * @author Michael Baylis
 *
 */
public class FrameworkResultArchiveStore implements IResultArchiveStore {

    private final IResultArchiveStoreService rasService;

    public FrameworkResultArchiveStore(IFramework framework, IResultArchiveStoreService rasService) { // NOSONAR
        this.rasService = rasService;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IResultArchiveStore#writeLog(java.lang.String)
     */
    @Override
    public void writeLog(@NotNull String message) throws ResultArchiveStoreException {
        this.rasService.writeLog(message);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IResultArchiveStore#writeLog(java.util.List)
     */
    @Override
    public void writeLog(@NotNull List<String> messages) throws ResultArchiveStoreException {
        this.rasService.writeLog(messages);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IResultArchiveStore#updateTestStructure(io.ejat.
     * framework.spi.teststructure.ITestStructure)
     */
    @Override
    public void updateTestStructure(@NotNull ITestStructure testStructure) throws ResultArchiveStoreException {
        this.rasService.updateTestStructure(testStructure);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IResultArchiveStore#getStoredArtifactsRoot()
     */
    @Override
    public Path getStoredArtifactsRoot() {
        return this.rasService.getStoredArtifactsRoot();
    }

}
