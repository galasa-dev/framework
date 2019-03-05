package io.ejat.framework.internal.ras.directory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;

import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.IResultArchiveStoreService;
import io.ejat.framework.spi.ResultArchiveStoreException;
import io.ejat.framework.spi.teststructure.ITestStructure;

/**
 * A RAS Service for storing the result archive store
 *
 * @author Michael Baylis
 *
 */
@Component(service = { IResultArchiveStoreService.class })
public class DirectoryResultArchiveStoreService implements IResultArchiveStoreService {

    private static final Charset           UTF8 = Charset.forName("utf-8");

    private IFramework                     framework;                      // NOSONAR
    private URI                            rasUri;
    private Path                           baseDirectory;

    private Path                           runDirectory;
    private Path                           testStructureFile;
    private Path                           runLog;

    private final Gson                     gson = new Gson();

    private DirectoryRASFileSystemProvider provider;

    /*
     * (non-Javadoc)
     *
     * @see
     * io.ejat.framework.spi.IResultArchiveStoreService#initialise(io.ejat.framework
     * .spi.IFrameworkInitialisation)
     */
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws ResultArchiveStoreException {
        this.framework = frameworkInitialisation.getFramework();

        // *** See if this RAS is to be activated, will eventually allow multiples of
        // itself
        final List<URI> rasUris = frameworkInitialisation.getResultArchiveStoreUris();
        for (final URI uri : rasUris) {
            if ("file".equals(uri.getScheme())) {
                if (this.rasUri != null) {
                    throw new ResultArchiveStoreException(
                            "The Directory RAS currently does not support multiple instances of itself");
                }
                this.rasUri = uri;
            }
        }

        // *** Create the base RAS directory
        this.baseDirectory = Paths.get(this.rasUri);
        try {
            Files.createDirectories(this.baseDirectory);
        } catch (final Exception e) {
            throw new ResultArchiveStoreException(
                    "Unable to create the RAS base directory '" + this.baseDirectory + "'", e);
        }

        // *** Get the runid to create the directory
        final String runid = this.framework.getTestRunId();
        if (runid == null) {
            throw new UnsupportedOperationException("Currently do not support running outside of a test run");
        }
        setRasRun(runid);

        // *** Set the locations of the standard files
        this.testStructureFile = this.runDirectory.resolve("structure.json");
        this.runLog = this.runDirectory.resolve("run.log");

        // *** Create an empty run log so we append it
        try {
            Files.createFile(this.runLog);
        } catch (final IOException e) {
            throw new ResultArchiveStoreException("Unable to create Run Log", e);
        }

        // *** Setup the provider to do all the work
        try {
            this.provider = new DirectoryRASFileSystemProvider(this.runDirectory);
        } catch (final IOException e) {
            throw new ResultArchiveStoreException("Unable to create the RAS Provider", e);
        }

        frameworkInitialisation.registerResultArchiveStoreService(this);
    }

    /**
     * Setup the run directory
     *
     * @param runid       - the id of the run
     * @param testRunName - the name of the run
     * @throws ResultArchiveStoreException - if we can't create the directories
     */
    private void setRasRun(String runid) throws ResultArchiveStoreException {
        this.runDirectory = this.baseDirectory.resolve(runid);
        try {
            Files.createDirectories(this.runDirectory);
            Files.createDirectories(this.runDirectory.resolve("artifacts"));
        } catch (final IOException e) {
            throw new ResultArchiveStoreException("Unable to create the RAS run directory '" + this.runDirectory + "'",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IResultArchiveStore#writeLog(java.lang.String)
     */
    @Override
    public void writeLog(@NotNull String message) throws ResultArchiveStoreException {
        Objects.requireNonNull(message);

        if (!message.endsWith("\n")) {
            message = message + "\n";
        }
        try {
            Files.write(this.runLog, message.getBytes(UTF8), StandardOpenOption.APPEND);
        } catch (final Exception e) {
            throw new ResultArchiveStoreException("Unable to write message to run log", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IResultArchiveStore#writeLog(java.util.List)
     */
    @Override
    public void writeLog(@NotNull List<String> messages) throws ResultArchiveStoreException {
        Objects.requireNonNull(messages);

        final StringBuilder sb = new StringBuilder();
        for (final String message : messages) {
            sb.append(message);
            if (!message.endsWith("\n")) {
                sb.append("\n");
            }
        }
        try {
            Files.write(this.runLog, sb.toString().getBytes(UTF8), StandardOpenOption.APPEND);
        } catch (final Exception e) {
            throw new ResultArchiveStoreException("Unable to write messages to run log", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IResultArchiveStore#updateTestStructure(io.ejat.
     * framework.spi.teststructure.ITestStructure)
     */
    @Override
    public void updateTestStructure(@NotNull ITestStructure testStructure) throws ResultArchiveStoreException {
        try {
            final String json = this.gson.toJson(testStructure);
            Files.write(this.testStructureFile, json.getBytes(UTF8));
        } catch (final Exception e) {
            throw new ResultArchiveStoreException("Unable to write the test structure", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see io.ejat.framework.spi.IResultArchiveStore#getStoredArtifactsRoot()
     */
    @Override
    public Path getStoredArtifactsRoot() {
        return this.provider.getActualFileSystem().getPath("/");
    }

}
