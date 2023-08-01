/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileSystem implements IFileSystem {
    public FileSystem() {
    }

    @Override
    public void createDirectories(Path folderPath) throws IOException {
        Files.createDirectories(folderPath);
    }

    @Override
    public void createFile(Path filePath) throws IOException {
        Files.createFile(filePath);
    }

    @Override
    public boolean exists(Path pathToFolderOrFile ) {
        return pathToFolderOrFile.toFile().exists();
    }

    @Override
    public boolean isRegularFile(Path filePath) {
        return Files.isRegularFile(filePath);
    }

    @Override
    public boolean isDirectory(Path filePath) {
        return Files.isDirectory(filePath);
    }

    @Override
    public Stream<Path> walk(Path folderPath) throws IOException {
        return Files.walk(folderPath);
    }

    @Override
    public long size(Path folderPath) throws IOException {
        return Files.size(folderPath);
    }

    @Override
    public InputStream newInputStream(Path folderPath) throws IOException {
        return Files.newInputStream(folderPath);
    }

    public String probeContentType(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        } else if (path.toString().endsWith(".properties")) {
            contentType = "text/plain";
        }
        return contentType;
    }
}
