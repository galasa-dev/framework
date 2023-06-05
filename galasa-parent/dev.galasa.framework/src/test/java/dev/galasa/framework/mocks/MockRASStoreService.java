/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.mocks;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class MockRASStoreService implements IResultArchiveStoreService{

    private Map<String,String> properties ;
    private boolean isFast ;

    public MockRASStoreService( Map<String,String> properties , boolean isFast) {
        this.properties = properties ;
        this.isFast = isFast ;
    }

    @Override
    public void writeLog(@NotNull String message) throws ResultArchiveStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'writeLog'");
    }

    @Override
    public void writeLog(@NotNull List<String> messages) throws ResultArchiveStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'writeLog'");
    }

    @Override
    public void updateTestStructure(@NotNull TestStructure testStructure) throws ResultArchiveStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'updateTestStructure'");
    }

    @Override
    public Path getStoredArtifactsRoot() {
        throw new UnsupportedOperationException("Unimplemented method 'getStoredArtifactsRoot'");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Unimplemented method 'flush'");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    @Override
    public @NotNull List<IResultArchiveStoreDirectoryService> getDirectoryServices() {
        throw new UnsupportedOperationException("Unimplemented method 'getDirectoryServices'");
    }

    @Override
    public String calculateRasRunId() {
        throw new UnsupportedOperationException("Unimplemented method 'calculateRasRunId'");
    }

    @Override
    public boolean isArtifactRootOnFastDisk() {
        return this.isFast;
    }
    
}
