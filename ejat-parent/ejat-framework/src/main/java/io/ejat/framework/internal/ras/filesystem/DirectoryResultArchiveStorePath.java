package io.ejat.framework.internal.ras.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

public class DirectoryResultArchiveStorePath implements Path {

	private final DirectoryResultArchiveStoreFileSystem fileSystem;
	private final List<String> nameElements = new ArrayList<>();
	private final boolean absolute;

	protected DirectoryResultArchiveStorePath(@NotNull DirectoryResultArchiveStoreFileSystem fileSystem,
			String path) {
		this.fileSystem = fileSystem;

		//*** Normalise the path name by stripping out double // and any trailing /
		while(path.contains("//")) {
			path = path.replaceAll("\\Q//\\E", "/");
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() -1);
		}
		this.absolute   = path.startsWith("/");

		//*** Break the path into elements
		int firstChar = -1;
		for(int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == '/') {
				if (firstChar != -1) {
					nameElements.add(path.substring(firstChar, i));
					firstChar = -1;
				} else {
					if (firstChar == -1) {
						firstChar = i;
					}
				}
			}
		}
		if (firstChar > 0) {
			nameElements.add(path.substring(firstChar, path.length()));
		}

		//*** Validate the elements
		for(String part : nameElements) {
			if (".".equals(part)) {
				throw new InvalidPathException(path, "Path parts of '.' are not allowed");
			}
			if ("..".equals(part)) {
				throw new InvalidPathException(path, "Path parts of '..' are not allowed");
			}
			if (part.contains("~")) {
				throw new InvalidPathException(path, "Path parts with '~' are not allowed");
			}
		}

	}

	public DirectoryResultArchiveStorePath(DirectoryResultArchiveStoreFileSystem fileSystem, boolean absolute,
			List<String> nameElements, int start, int end) {
		this.fileSystem = fileSystem;
		this.absolute = absolute;
		for(int i = start; i < end; i++) {
			this.nameElements.add(nameElements.get(i));
		}
	}

	@Override
	public FileSystem getFileSystem() {
		return this.fileSystem;
	}

	@Override
	public boolean isAbsolute() {
		return this.absolute;
	}

	@Override
	public Path getRoot() {
		return new DirectoryResultArchiveStorePath(fileSystem, "/");
	}

	@Override
	public Path getFileName() {
		if (this.nameElements.isEmpty()) {
			return null;
		}

		return new DirectoryResultArchiveStorePath(fileSystem, nameElements.get(nameElements.size() -1));
	}

	@Override
	public Path getParent() {
		if (this.nameElements.size() <= 1) {
			return null;
		}
		return new DirectoryResultArchiveStorePath(fileSystem, absolute, nameElements, 0, nameElements.size() - 1);
	}

	@Override
	public int getNameCount() {
		return this.nameElements.size();
	}

	@Override
	public Path getName(int index) {
		if (index < 0 || index >= this.nameElements.size()) {
			return null;
		}
		return new DirectoryResultArchiveStorePath(fileSystem, this.nameElements.get(index));
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		if (beginIndex < 0 || beginIndex >= this.nameElements.size()) {
			return null;
		}
		if (endIndex < 0 || endIndex >= this.nameElements.size()) {
			return null;
		}
		if (endIndex <= beginIndex) {
			return null;
		}
		return new DirectoryResultArchiveStorePath(fileSystem, absolute, nameElements, beginIndex, endIndex);
	}

	@Override
	public boolean startsWith(Path other) {
		DirectoryResultArchiveStorePath o = checkPath(other);

		if (o.nameElements.size() > this.nameElements.size()) {
			return false;
		}

		for(int i = 0; i < o.nameElements.size(); i++) {
			if (!o.nameElements.get(i).equals(this.nameElements.get(i))) {
				return false;
			}
		}
		return true;
	}

	private DirectoryResultArchiveStorePath checkPath(Path path) {
		if (path == null)
			throw new NullPointerException();
		if (!(path instanceof DirectoryResultArchiveStorePath))
			throw new ProviderMismatchException();
		return (DirectoryResultArchiveStorePath) path;
	}


	@Override
	public boolean startsWith(String other) {
		return startsWith(fileSystem.getPath(other));
	}

	@Override
	public boolean endsWith(Path other) {
		DirectoryResultArchiveStorePath o = checkPath(other);

		if (o.nameElements.size() > this.nameElements.size()) {
			return false;
		}

		for(int i = o.nameElements.size() - 1, j = this.nameElements.size() - 1; i >= 0; i--, j--) {
			if (!o.nameElements.get(i).equals(this.nameElements.get(j))) {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean endsWith(String other) {
		return endsWith(fileSystem.getPath(other));
	}

	@Override
	public Path normalize() {
		// No Normalisation to do as . and .. are not possible
		return this;
	}

	@Override
	public Path resolve(Path other) {
		DirectoryResultArchiveStorePath o = checkPath(other);

		if (o.absolute) {
			return o;
		}

		ArrayList<String> combined = new ArrayList<>(this.nameElements);
		combined.addAll(o.nameElements);

		return new DirectoryResultArchiveStorePath(fileSystem, absolute, combined, 0, combined.size() -1);
	}

	@Override
	public Path resolve(String other) {
		return resolve(fileSystem.getPath(other));
	}

	@Override
	public Path resolveSibling(Path other) {
		DirectoryResultArchiveStorePath o = checkPath(other);

		if (o.absolute || this.nameElements.isEmpty()) {
			return o;
		}
		ArrayList<String> combined = new ArrayList<>(this.nameElements);
		combined.remove(combined.size() -1);
		combined.addAll(o.nameElements);

		return new DirectoryResultArchiveStorePath(fileSystem, absolute, combined, 0, combined.size() -1);
	}

	@Override
	public Path resolveSibling(String other) {
		return resolveSibling(fileSystem.getPath(other));
	}

	@Override
	public Path relativize(Path other) {
		DirectoryResultArchiveStorePath o = checkPath(other);
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI toUri() {
		try {
			return new URI(DirectoryResultArchiveStoreFileSystemProvider.SCHEME + ":" + toAbsolutePath().toString());
		} catch(Exception e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean prefixSeperator = absolute;
		for(String element : nameElements) {
			if (prefixSeperator) {
				sb.append("/");
			}
			sb.append(element);
			prefixSeperator = true;
		}
		return super.toString();
	}

	@Override
	public Path toAbsolutePath() {
		if (absolute) {
			return this;
		}

		return new DirectoryResultArchiveStorePath(fileSystem, true, nameElements, 0, nameElements.size() - 1);
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		return null;
	}

	@Override
	public File toFile() {
		throw new UnsupportedOperationException("Unable to translate to a File");
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException("Watching is not supported with this filesystem");
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
		throw new UnsupportedOperationException("Watching is not supported with this filesystem");
	}

	@Override
	public Iterator<Path> iterator() {
		ArrayList<Path> it = new ArrayList<>();
		for(String element : nameElements) {
			it.add(new DirectoryResultArchiveStorePath(fileSystem, element));
		}
		return it.iterator();
	}

	@Override
	public int compareTo(Path other) {
		// TODO Auto-generated method stub
		return 0;
	}

}
