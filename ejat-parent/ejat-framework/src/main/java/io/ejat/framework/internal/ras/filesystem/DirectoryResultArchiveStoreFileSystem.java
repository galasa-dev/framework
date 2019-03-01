package io.ejat.framework.internal.ras.filesystem;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Set;

public class DirectoryResultArchiveStoreFileSystem extends FileSystem {
	
	private final Path baseRunDirectory;
	private final Path artifactDirectory;
	
	private final DirectoryResultArchiveStoreFileSystemProvider fileSystemProvider;
	
	public DirectoryResultArchiveStoreFileSystem(Path baseRunDirectory) throws IOException {
		this.baseRunDirectory = baseRunDirectory;
		this.artifactDirectory = this.baseRunDirectory.resolve("artifacts");
		if (!Files.exists(artifactDirectory)) {
			Files.createDirectory(artifactDirectory);
		}
		
		this.fileSystemProvider = new DirectoryResultArchiveStoreFileSystemProvider(this.artifactDirectory);
	}

	@Override
	public FileSystemProvider provider() {
		return this.fileSystemProvider;
	}

	@Override
	public void close() throws IOException {
		// Dont do anything
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getSeparator() {
		return "/";
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		ArrayList<FileStore> fileStores = new ArrayList<>();
		fileStores.add(this.fileSystemProvider.getActualFireStore());
		return fileStores;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Path getPath(String first, String... more) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public WatchService newWatchService() throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
