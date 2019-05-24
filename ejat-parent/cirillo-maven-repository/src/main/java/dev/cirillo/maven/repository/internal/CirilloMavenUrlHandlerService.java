package dev.cirillo.maven.repository.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

import dev.cirillo.maven.repository.IMavenRepository;

@Component(service={URLStreamHandlerService.class}, property= {URLConstants.URL_HANDLER_PROTOCOL + "=mvn"})
public class CirilloMavenUrlHandlerService extends AbstractURLStreamHandlerService {

	private final static Log logger = LogFactory.getLog(CirilloMavenUrlHandlerService.class);
	private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuuMMddHHmmss");

	@Reference
	private IMavenRepository cirilloRepository;
	
	private final Path localCirilloRepository = Paths.get(System.getProperty("user.home") + "/.cirillo/mavenrepo");
	
	@Override
	public URLConnection openConnection(URL arg0) throws IOException {
		
		String[] parts = arg0.getPath().split("/");
		if (parts.length != 4) {
			throw new MalformedURLException("Must have 4 parts in the maven artifact reference - " + arg0);
		}
		
		String groupID = parts[0].trim();
		String artifactID = parts[1].trim();
		String version = parts[2].trim();
		String type = parts[3].trim();
		
		if (groupID.isEmpty()) {
			throw new MalformedURLException("groupid is missing - " + arg0);
		}
		if (artifactID.isEmpty()) {
			throw new MalformedURLException("groupid is missing - " + arg0);
		}
		if (version.isEmpty()) {
			throw new MalformedURLException("groupid is missing - " + arg0);
		}
		if (type.isEmpty()) {
			throw new MalformedURLException("groupid is missing - " + arg0);
		}
		
		URL result = fetchArtifact(localCirilloRepository, groupID, artifactID, version, type);
		if (result == null) {
			throw new IOException("Unable to local maven artifact " + arg0);
		}
		
		return result.openConnection();
	}

	private URL fetchArtifact(Path localCirilloRepository, String groupid, String artifactid, String version, String type) throws IOException {
		logger.trace("Resolving maven artifact " + groupid + ":" + artifactid + ":" + version + ":" + type);

		URL localRepository = cirilloRepository.getLocalRepository();

		//*** Check the local repository first, if the file exists,  use it from there
		if (localRepository != null) {
			try {
				URL localFile = buildArtifactUrl(localRepository, groupid, artifactid, version, buildArtifactFilename(artifactid, version, type));
				Path pathLocalFile = Paths.get(localFile.toURI()); 
				if (Files.exists(pathLocalFile)) {
					logger.trace("Found in local repository at " + localFile.toExternalForm());
					return localFile;
				}
			} catch(Exception e) {
				throw new IOException("Problem with local maven repository");
			}
		}

		if (cirilloRepository.getRemoteRepositories() == null) {
			return null;
		}

		Path localGroupDirectory = localCirilloRepository.resolve(groupid);
		Files.createDirectories(localGroupDirectory);
		String targetFilename = artifactid + "-" + version + "." + type;
		Path localArtifact = localGroupDirectory.resolve(targetFilename);

		if (version.endsWith("-SNAPSHOT")) {
			return fetchSnapshotArtifact(localArtifact, groupid, artifactid, version, type);
		} else {
			return fetchReleaseArtifact(localArtifact, groupid, artifactid, version, type);
		}
	}

	private URL fetchSnapshotArtifact(Path localArtifact, String groupid, String artifactid, String version,	String type) throws IOException {
		Path localTimestamp = localArtifact.resolveSibling(localArtifact.getFileName().toString() + ".lastupdated");

		long lastupdated = -1;

		//*** Get the local last updated timestamp if it exists
		if (Files.exists(localTimestamp) && Files.exists(localArtifact)) {
			try {
				lastupdated = Long.parseLong(new String(Files.readAllBytes(localTimestamp), "utf-8"));
			} catch(Exception e) {}
		}

		//*** has it been updated today
		LocalDate today = LocalDate.now();
		long startOfDay = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		if (lastupdated >= startOfDay) {
			logger.info("Downloaded snapshot is still valid");
			return localArtifact.toUri().toURL();
		}

		logger.debug("Looking for updated snapshot of " + groupid + ":" + artifactid + ":" + version + ":" + type);

		for(URL remoteRepository : cirilloRepository.getRemoteRepositories()) {
			URL found = retrieveSnapshot(remoteRepository, lastupdated, localArtifact, localTimestamp, groupid, artifactid, version, type);
			if (found != null) {
				return found;
			}
		}

		return null;
	}

	private static URL retrieveSnapshot(URL repository, 
			long lastupdated, 
			Path localArtifact, 
			Path localTimestamp, 
			String groupid,
			String artifactid, 
			String version, 
			String type) throws IOException {


		Path tempMetadata = Files.createTempFile("metadata", ".xml");
		try {
			URL urlRemoteFile = buildArtifactUrl(repository, groupid, artifactid, version, "maven-metadata.xml");
			logger.debug("Attempting to download " + urlRemoteFile);

			URLConnection connection = urlRemoteFile.openConnection();
			connection.setDoOutput(false);
			connection.connect();
			Files.copy(connection.getInputStream(), tempMetadata, StandardCopyOption.REPLACE_EXISTING);
		} catch(FileNotFoundException e) {
			Files.delete(tempMetadata);
			return null;
		}
		
		String snapshotSuffix = null;
		long updatedTime = 0;
		try {
			MetadataXpp3Reader reader = new MetadataXpp3Reader();
			Metadata metadata = reader.read(Files.newInputStream(tempMetadata));

			Versioning versioning = metadata.getVersioning();
			if (versioning != null) {
				String sUpdatedtime = versioning.getLastUpdated();
				LocalDateTime updated = LocalDateTime.parse(sUpdatedtime, dtf);
				updatedTime = updated.toEpochSecond(ZoneOffset.UTC);

				if (updatedTime < lastupdated) {
					logger.debug("Snapshot is up to date");
					return localArtifact.toUri().toURL();
				}


				Snapshot snapshot = versioning.getSnapshot();
				if (snapshot == null) {
					return null;
				}

				if (snapshot.isLocalCopy()) {
					snapshotSuffix = version;
				} else {
					snapshotSuffix = version.replace("SNAPSHOT", snapshot.getTimestamp() + "-" + snapshot.getBuildNumber());				
				}
			}
		} catch(XmlPullParserException e) {
			return null;
		} finally {
			Files.delete(tempMetadata);
		}

		URL urlRemoteFile = buildArtifactUrl(repository, groupid, artifactid, version, buildArtifactFilename(artifactid, snapshotSuffix, type));
		logger.debug("Attempting to download " + urlRemoteFile);

		URLConnection connection = urlRemoteFile.openConnection();
		connection.setDoOutput(false);
		connection.connect();

		try {
			Files.copy(connection.getInputStream(), localArtifact, StandardCopyOption.REPLACE_EXISTING);
		} catch(FileNotFoundException e) {
			return null;
		}
		
		logger.trace("Snapshot artifact downloaded from " + urlRemoteFile);

		Files.write(localTimestamp, Long.toString(Instant.now().getEpochSecond()).getBytes());

		return localArtifact.toUri().toURL();
	}

	private URL fetchReleaseArtifact(Path localArtifact, String groupid, String artifactid, String version, String type) throws IOException {
		if (Files.exists(localArtifact)) {
			logger.trace("Release artifact already in Cirillo repository");
			return localArtifact.toUri().toURL();
		}



		for(URL remoteRepository : cirilloRepository.getRemoteRepositories()) {
			if (getArtifact(remoteRepository, localArtifact, groupid, artifactid, version, type)) {
				return localArtifact.toUri().toURL();
			}		
		}
		
		return null;
	}


	private static boolean getArtifact(URL repository, Path localArtifact, String groupid, String artifactid, String version, String type) throws IOException {
		logger.debug("Checking " + repository);

		//*** Read the artifact
		URL urlRemoteFile = buildArtifactUrl(repository, groupid, artifactid, version, localArtifact.getFileName().toString());
		logger.debug("Attempting to download " + urlRemoteFile);
		URLConnection connection = urlRemoteFile.openConnection();
		connection.setDoOutput(false);
		connection.connect();

		try {
			Files.copy(connection.getInputStream(), localArtifact, StandardCopyOption.REPLACE_EXISTING);
		} catch(FileNotFoundException e) {
			return false;
		}

		logger.trace("Release artifact downloaded from " + urlRemoteFile);

		return true;
	}

	private static URL buildArtifactUrl(URL repository, String groupid, String artifactid, String version, String filename) throws IOException {
		String groupidDirectory = groupid.replaceAll("\\.", "/");

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(repository.toExternalForm());
		stringBuilder.append("/");
		stringBuilder.append(groupidDirectory);
		stringBuilder.append("/");
		stringBuilder.append(artifactid);
		stringBuilder.append("/");
		stringBuilder.append(version);
		stringBuilder.append("/");
		stringBuilder.append(filename);

		//*** Read the artifact
		return new URL(stringBuilder.toString());
	}

	private static String buildArtifactFilename(String artifactid, String version, String filename) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(artifactid);
		stringBuilder.append("-");
		stringBuilder.append(version);
		stringBuilder.append(".");
		stringBuilder.append(filename);

		return stringBuilder.toString();
	}
}
