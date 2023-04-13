/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework;

import java.io.IOException;
import java.nio.file.Path;

public interface IFileSystem {

    void createDirectories(Path folderPath ) throws IOException ;

    void createFile(Path filePath) throws IOException ;

    boolean exists(Path pathToFolderOrFile);
    
}
