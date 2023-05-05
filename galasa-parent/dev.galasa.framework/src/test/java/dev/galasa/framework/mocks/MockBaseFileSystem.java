/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.mocks;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Stream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import dev.galasa.framework.IFileSystem;


public class MockBaseFileSystem extends FileSystem implements IFileSystem {

    @Override
    public void createDirectories(Path folderPath) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'createDirectories'");
    }

    @Override
    public void createFile(Path filePath) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'createFile'");
    }

    @Override
    public boolean exists(Path pathToFolderOrFile) {
        throw new UnsupportedOperationException("Unimplemented method 'exists'");
    }

    @Override
    public boolean isRegularFile(Path filePath) {
        throw new UnsupportedOperationException("Unimplemented method 'isRegularFile'");
    }

    @Override
    public boolean isDirectory(Path filePath) {
        throw new UnsupportedOperationException("Unimplemented method 'isDirectory'");
    }

    @Override
    public Stream<Path> walk(Path folderPath) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'walk'");
    }

    @Override
    public FileSystemProvider provider() {
        throw new UnsupportedOperationException("Unimplemented method 'provider'");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Unimplemented method 'isOpen'");
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException("Unimplemented method 'isReadOnly'");
    }

    @Override
    public String getSeparator() {
        throw new UnsupportedOperationException("Unimplemented method 'getSeparator'");
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        throw new UnsupportedOperationException("Unimplemented method 'getRootDirectories'");
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException("Unimplemented method 'getFileStores'");
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        throw new UnsupportedOperationException("Unimplemented method 'supportedFileAttributeViews'");
    }

    @Override
    public Path getPath(String first, String... more) {
        throw new UnsupportedOperationException("Unimplemented method 'getPath'");
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException("Unimplemented method 'getPathMatcher'");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("Unimplemented method 'getUserPrincipalLookupService'");
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'newWatchService'");
    }
}