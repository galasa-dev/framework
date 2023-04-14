/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
