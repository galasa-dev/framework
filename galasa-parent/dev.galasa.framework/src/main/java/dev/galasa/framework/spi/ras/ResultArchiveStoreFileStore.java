/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

/**
 * Dummy File Store if needed
 *
 * @author Michael Baylis
 *
 */
public class ResultArchiveStoreFileStore extends FileStore {

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#name()
     */
    @Override
    public String name() {
        return "ras";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#type()
     */
    @Override
    public String type() {
        return "ras";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#getTotalSpace()
     */
    @Override
    public long getTotalSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#getUsableSpace()
     */
    @Override
    public long getUsableSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#getUnallocatedSpace()
     */
    @Override
    public long getUnallocatedSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#supportsFileAttributeView(java.lang.Class)
     */
    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#supportsFileAttributeView(java.lang.String)
     */
    @Override
    public boolean supportsFileAttributeView(String name) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#getFileStoreAttributeView(java.lang.Class)
     */
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileStore#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String attribute) throws IOException {
        return null;
    }

}
