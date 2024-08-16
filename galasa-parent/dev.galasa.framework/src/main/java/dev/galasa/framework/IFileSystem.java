/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.stream.Stream;

import dev.galasa.ResultArchiveStoreContentType;

public interface IFileSystem {

    void createDirectories(Path folderPath ) throws IOException;

    void createFile(Path filePath) throws IOException;

    boolean exists(Path pathToFolderOrFile);

    boolean isRegularFile(Path filePath);

    boolean isDirectory(Path filePath);
    
    Stream<Path> walk(Path folderPath) throws IOException;

    long size(Path folderPath) throws IOException;

    String probeContentType(Path artifactPath) throws IOException;

    InputStream newInputStream(Path folderPath) throws IOException;

    Path createFile(Path path, FileAttribute<?>... attrs) throws IOException;

    void write(Path rasProperties, byte[] bytes) throws IOException ;
}
