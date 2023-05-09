/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.mocks;

import java.io.IOException;
import java.nio.file.Path;

import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Stream;
import java.nio.file.DirectoryStream.Filter;


/**
 * - A directory is a file system.
 * - Each directory holds a map of nodes in that directory.
 * - Each node could be a file, or another directory.
 * - Each file system knows where it is mounted.
 */
public class MockFileSystem extends MockBaseFileSystem {

    private MockFileSystem parentFileSystem ;
    private static class Node {
        // Path path;
        boolean isFolder ;
        byte[] contents ;
    }

    // A map of parts of the path, and the node it relates to.
    private Map<String,Node> files = new HashMap<>();


    public MockFileSystem() {
        parentFileSystem = null ;
    }

    public MockFileSystem( MockFileSystem parentFileSystem) {
        this.parentFileSystem = parentFileSystem ;
    }



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
    public boolean isDirectory(Path filePath) {
        return files.get(filePath.toString()).isFolder;
    }

    @Override
    public Stream<Path> walk(Path folderPath) {
        return files.keySet().stream()
            .filter(path -> path.startsWith(folderPath.toString()))
            .map(path -> new MockPath(path.toString(), this));
    }

    @Override
    public FileSystemProvider provider() {
        return new MockFileSystemProvider(this);
    }


    public List<Path> getListOfFiles(String directory, Filter<? super Path> filter) throws IOException {
        List<Path> resultPaths = new ArrayList<>();
    
        for (String pathStr : files.keySet()) {
            // Check if this path is to a direct child of the given directory
            if (pathStr.startsWith(directory + "/") && !(pathStr.substring(directory.length() + 1).contains("/"))) {
                MockPath mockPath = new MockPath(pathStr, this);

                if (filter.accept(mockPath)) {
                    resultPaths.add(mockPath);
                }
            }
        }
        return resultPaths;
    }
}
