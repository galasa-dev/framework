/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.mocks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import dev.galasa.framework.IFileSystem;

public class MockFileSystem implements IFileSystem {

    private static class Node {
        // Path path;
        boolean isFolder ;
        byte[] contents ;
    }

    private Map<String,Node> files = new HashMap<>();

    public boolean isFolder(Path folderPath) {
        boolean isFolder = false ;
        Node node = files.get(folderPath.toString());
        if (node != null) {
            isFolder = node.isFolder;
        }
        return isFolder;
    }

    public byte[] getContentsAsBytes(Path filePath) {
        Node node = files.get(filePath.toString());
        byte[] results = null; 
        if (node != null) {
            results = node.contents;
        }
        return results;
    }

    public String getContentsAsString(Path filePath) {
        byte[] bytes = getContentsAsBytes(filePath);
        String results = null;
        if (bytes != null ) {
            results = new String(bytes);
        }
        return results;
    }

    @Override
    public void createDirectories(Path folderPath) throws IOException {
        if (folderPath!=null) {
            if(!exists(folderPath)) {
                // Recursively make sure the parent folder exists.
                Path parent = folderPath.getParent();
                createDirectories(parent);
                // Now create this folder.
                createNode(folderPath, true);
            }
        }
    }

    @Override
    public void createFile(Path path) throws IOException {
        if (exists(path)) {
            throw new IOException("File "+path.toString()+" already exists!");
        }
        createNode(path, false);
    }

    private void createNode(Path path, boolean isFolder) {
        Node node = new Node();
        node.isFolder = isFolder ;
        node.contents = new byte[0];
        // node.path = path ;
        files.put(path.toString(),node);
    }

    @Override
    public boolean exists(Path pathToFolderOrFile) {
        boolean isExists ;
        Node node = this.files.get(pathToFolderOrFile.toString());
        if (node == null) {
            isExists = false ;
        } else {
            isExists = true ;
        }
        return isExists;
    }

    @Override
    public boolean isRegularFile(Path filePath) {
        return !files.get(filePath.toString()).isFolder;
    }

    @Override
    public Stream<Path> walk(Path folderPath) {
        return files.keySet().stream()
            .filter(path -> path.startsWith(folderPath.toString()))
            .map(MockPath::new);
    }
}
