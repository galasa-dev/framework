package io.ejat.framework.spi.ras;

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

public class ResultArchiveStorePath implements Path {

	private final FileSystem fileSystem;
	private final List<String> nameElements = new ArrayList<>();
	private final boolean absolute;

	protected ResultArchiveStorePath(@NotNull FileSystem fileSystem,
			String path) {
		this.fileSystem = fileSystem;

		if (path == null) {
			throw new NullPointerException();
		}

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
				} 
			} else {
				if (firstChar == -1) {
					firstChar = i;
				}
			}
		}

		if (firstChar >= 0) {
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

		//*** finally,  check it can be converted to an URI
		try {
			toUri();
		} catch(AssertionError e) {
			throw new AssertionError("Invalid path, would have conversion to URI", e);
		}
	}

	private ResultArchiveStorePath(FileSystem fileSystem, boolean absolute,
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
		return new ResultArchiveStorePath(fileSystem, true, new ArrayList<>(), 0, 0);
	}

	@Override
	public Path getFileName() {
		if (this.nameElements.isEmpty()) {
			return null;
		}

		return new ResultArchiveStorePath(fileSystem, nameElements.get(nameElements.size() -1));
	}

	@Override
	public Path getParent() {
		if (this.nameElements.size() <= 1) {
			return null;
		}
		return new ResultArchiveStorePath(fileSystem, absolute, nameElements, 0, nameElements.size() - 1);
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
		return new ResultArchiveStorePath(fileSystem, this.nameElements.get(index));
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		if (endIndex <= beginIndex) {
			return null;
		}
		if (beginIndex < 0 || beginIndex >= this.nameElements.size()) {
			return null;
		}
		if (endIndex >= this.nameElements.size()) {
			return null;
		}

		boolean newAbolute = this.absolute;
		if (beginIndex > 0) {
			newAbolute = false;
		}
		return new ResultArchiveStorePath(fileSystem, newAbolute, nameElements, beginIndex, endIndex);
	}

	@Override
	public boolean startsWith(Path other) {
		ResultArchiveStorePath o = checkPath(other);

		if (this.absolute != o.absolute) {
			return false;
		}

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

	private ResultArchiveStorePath checkPath(Path path) {
		if (path == null)
			throw new NullPointerException();
		if (!(path instanceof ResultArchiveStorePath))
			throw new ProviderMismatchException();
		return (ResultArchiveStorePath) path;
	}


	@Override
	public boolean startsWith(String other) {
		return startsWith(new ResultArchiveStorePath(fileSystem, other));
	}

	@Override
	public boolean endsWith(Path other) {
		ResultArchiveStorePath o = checkPath(other);

		if (o.nameElements.size() > this.nameElements.size()) {
			return false;
		}

		if (o.nameElements.size() == this.nameElements.size()) {
			if (o.absolute != this.absolute) {
				return false;
			}
		}

		for(int i = o.nameElements.size() - 1, j = this.nameElements.size() - 1; i >= 0; i--, j--) {
			if (!o.nameElements.get(i).equals(this.nameElements.get(j))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean endsWith(String other) {
		return endsWith(new ResultArchiveStorePath(fileSystem, other));
	}

	@Override
	public Path normalize() {
		// No Normalisation to do as . and .. are not possible
		return this;
	}

	@Override
	public Path resolve(Path other) {
		ResultArchiveStorePath o = checkPath(other);

		if (o.absolute) {
			return o;
		}

		ArrayList<String> combined = new ArrayList<>(this.nameElements);
		combined.addAll(o.nameElements);

		return new ResultArchiveStorePath(fileSystem, absolute, combined, 0, combined.size());
	}

	@Override
	public Path resolve(String other) {
		return resolve(new ResultArchiveStorePath(fileSystem, other));
	}

	@Override
	public Path resolveSibling(Path other) {
		ResultArchiveStorePath o = checkPath(other);

		if (o.absolute || this.nameElements.isEmpty()) {
			return o;
		}
		ArrayList<String> combined = new ArrayList<>(this.nameElements);
		combined.remove(combined.size() -1);
		combined.addAll(o.nameElements);

		return new ResultArchiveStorePath(fileSystem, absolute, combined, 0, combined.size());
	}

	@Override
	public Path resolveSibling(String other) {
		return resolveSibling(new ResultArchiveStorePath(fileSystem, other));
	}

	@Override
	public Path relativize(Path other) {
		ResultArchiveStorePath o = checkPath(other);
		
		if (o.absolute && !this.absolute) {
			return o;
		}
		
		if (absolute && !o.absolute) {
			return null;
		}
		
		if (equals(o)) {
			return new ResultArchiveStorePath(fileSystem, "");
		}
		
		if (!o.startsWith(this)) {
			return null;
		}
		
		return new ResultArchiveStorePath(fileSystem, false, o.nameElements, this.nameElements.size(), o.nameElements.size());
	}

	@Override
	public URI toUri() {
		try {
			return new URI(fileSystem.provider().getScheme() + ":" + toAbsolutePath().toString());
		} catch(Exception e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public String toString() {
		if (this.absolute && this.nameElements.isEmpty()) {
			return "/";
		}

		StringBuilder sb = new StringBuilder();
		boolean prefixSeperator = absolute;
		for(String element : nameElements) {
			if (prefixSeperator) {
				sb.append("/");
			}
			sb.append(element);
			prefixSeperator = true;
		}
		return sb.toString();
	}

	@Override
	public Path toAbsolutePath() {
		if (absolute) {
			return this;
		}

		return new ResultArchiveStorePath(fileSystem, true, nameElements, 0, nameElements.size());
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		return null;
	}

	@Override
	public File toFile() {
		throw new UnsupportedOperationException("Unable to translate to a java.ioFile");
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
			it.add(new ResultArchiveStorePath(fileSystem, element));
		}
		return it.iterator();
	}

	@Override
	public int compareTo(Path other) {
		if (!(other instanceof ResultArchiveStorePath))
			throw new ProviderMismatchException();		
		ResultArchiveStorePath o = (ResultArchiveStorePath) other;

		if (this.nameElements.size() == 0 && o.nameElements.size() == 0) {
			return 0;
		}

		if (this.nameElements.size() == 0) {
			return -1;
		}

		if (o.nameElements.size() == 0) {
			return 1;
		}

		int maxSize = Math.max(this.nameElements.size(), o.nameElements.size());
		for(int i = 0; i < maxSize; i++) {
			if (i >= this.nameElements.size()) {
				return -1;
			}
			if (i >= o.nameElements.size()) {
				return 1;
			}

			int c = this.nameElements.get(i).compareTo(o.nameElements.get(i));

			if (c < 0) {
				return -1;
			}

			if (c > 0) {
				return 1;
			}
		}

		return 0;
	}

}
