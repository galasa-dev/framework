package dev.voras.framework.internal.ras.directory;

import dev.voras.framework.spi.ras.ResultArchiveStoreFileSystem;
import dev.voras.framework.spi.ras.ResultArchiveStoreFileSystemProvider;

/**
 * The Directory RAS File System, basically use the shared generic version
 *
 * @author Michael Baylis
 *
 */
public class DirectoryRASFileSystem extends ResultArchiveStoreFileSystem {

    public DirectoryRASFileSystem(ResultArchiveStoreFileSystemProvider fileSystemProvider) {
        super(fileSystemProvider);
    }

}
