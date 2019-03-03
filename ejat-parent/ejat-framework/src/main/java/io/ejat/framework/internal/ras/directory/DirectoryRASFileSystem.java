package io.ejat.framework.internal.ras.directory;

import io.ejat.framework.spi.ras.ResultArchiveStoreFileSystem;
import io.ejat.framework.spi.ras.ResultArchiveStoreFileSystemProvider;

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
