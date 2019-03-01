package io.ejat.framework.internal.ras.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

public class DirectoryResultArchiveStoreFileSystemProvider extends FileSystemProvider {
	
	public static final String SCHEME = "rasdir";
	
	private final Path artifactDirectory;
	
	private final DirectoryResultArchiveStoreFileStore fileStore = new DirectoryResultArchiveStoreFileStore();

	public DirectoryResultArchiveStoreFileSystemProvider(Path artifactDirectory) {
		this.artifactDirectory = artifactDirectory;
	}
	
	protected FileStore getActualFireStore() {
		return this.fileStore;
	}
	
	@Override
	public String getScheme() {
		return SCHEME;
	}

	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public FileSystem getFileSystem(URI uri) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Path getPath(URI uri) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void delete(Path path) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean isHidden(Path path) throws IOException {
		return false;
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		return new DirectoryResultArchiveStoreFileStore();
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
