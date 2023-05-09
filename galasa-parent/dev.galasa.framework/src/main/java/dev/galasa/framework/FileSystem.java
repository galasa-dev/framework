/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework;

import java.io.IOException;
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

    public String probeContentType(Path path) throws IOException {
        String contentType = null;
        if (path.toString().endsWith(".properties")) {
          contentType =  "text/plain";
        } else {
            contentType = Files.probeContentType(path);
        }
        return contentType;
    }
}
