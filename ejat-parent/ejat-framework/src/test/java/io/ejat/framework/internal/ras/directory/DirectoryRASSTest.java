package io.ejat.framework.internal.ras.directory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import io.ejat.ResultArchiveStoreContentType;
import io.ejat.ResultArchiveStoreFileAttributeView;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.ResultArchiveStoreException;
import io.ejat.framework.spi.teststructure.ITestStructure;

/**
 * Test the Directory based Result Archive Store
 *
 * @author Michael Baylis
 *
 */
public class DirectoryRASSTest {

    private Path                     rasDirectory;

    private IFramework               framework;
    private IFrameworkInitialisation frameworkInit;

    private static final String      runid   = "areallygoodid";
    private static final String      runname = "BOB1";

    @Before
    public void before() throws IOException {
        this.rasDirectory = Files.createTempDirectory("ejat_junit_ras_");

        this.framework = mock(IFramework.class);
        when(this.framework.getTestRunId()).thenReturn(runid);
        when(this.framework.getTestRunName()).thenReturn(runname);

        final ArrayList<URI> rasURIs = new ArrayList<>();
        rasURIs.add(this.rasDirectory.toUri());

        this.frameworkInit = mock(IFrameworkInitialisation.class);
        when(this.frameworkInit.getResultArchiveStoreUris()).thenReturn(rasURIs);
        when(this.frameworkInit.getFramework()).thenReturn(this.framework);
    }

    @After
    public void after() throws IOException {
        if (this.rasDirectory != null) {
            if (Files.exists(this.rasDirectory)) {
                FileUtils.deleteDirectory(this.rasDirectory.toFile());
            }
        }
    }

    @Test
    public void testStoredArtifacts() throws ResultArchiveStoreException, IOException {
        final DirectoryResultArchiveStoreService drass = new DirectoryResultArchiveStoreService();
        drass.initialise(this.frameworkInit);
        verify(this.frameworkInit).registerResultArchiveStoreService(drass);

        Assert.assertTrue("Run RAS directory should have been created", Files.exists(this.rasDirectory.resolve(runid)));
        Assert.assertTrue("Run RAS artifacts directory should have been created",
                Files.exists(this.rasDirectory.resolve(runid).resolve("artifacts")));

        final Path root = drass.getStoredArtifactsRoot();
        Assert.assertNotNull("RASS did not return a Stored Artifacts Root", root);

        final Path testFile = root.resolve("test.xml");

        Files.createFile(testFile, ResultArchiveStoreContentType.XML);
        Assert.assertTrue("text.xml should have been created",
                Files.exists(this.rasDirectory.resolve(runid).resolve("artifacts").resolve("test.xml")));

        Assert.assertEquals("Should be marked as XML", ResultArchiveStoreContentType.XML,
                Files.getFileAttributeView(testFile, ResultArchiveStoreFileAttributeView.class).getContentType());
    }

    @Test
    public void testTestStructure() throws ResultArchiveStoreException, UnsupportedEncodingException, IOException {
        final DirectoryResultArchiveStoreService drass = new DirectoryResultArchiveStoreService();
        drass.initialise(this.frameworkInit);

        final TestStructure writeStructure = new TestStructure();
        writeStructure.testData = "hello everyone";

        final Path pathStructure = this.rasDirectory.resolve(runid).resolve("structure.json");
        Assert.assertFalse("test structure file should not exist before write", Files.exists(pathStructure));

        drass.updateTestStructure(writeStructure);

        Assert.assertTrue("test structure file should now exist", Files.exists(pathStructure));

        final String json = new String(Files.readAllBytes(pathStructure), "utf-8");

        final Gson gson = new Gson();
        final TestStructure readStructure = gson.fromJson(json, TestStructure.class);

        Assert.assertEquals("Test structure data different", writeStructure.testData, readStructure.testData);
    }

    @Test
    public void testRunLog() throws ResultArchiveStoreException, IOException {
        final String message1 = "1st message\n";
        final String message2 = "2nd message";
        final ArrayList<String> messages = new ArrayList<>();
        messages.add("3nd Message\n");
        messages.add("All done");

        final DirectoryResultArchiveStoreService drass = new DirectoryResultArchiveStoreService();
        drass.initialise(this.frameworkInit);

        drass.writeLog(message1);
        drass.writeLog(message2);
        drass.writeLog(messages);

        final List<String> readMessages = Files.readAllLines(this.rasDirectory.resolve(runid).resolve("run.log"));

        Assert.assertEquals("message 1 wrong", message1, readMessages.get(0) + "\n");
        Assert.assertEquals("message 1 wrong", message2, readMessages.get(1));
        Assert.assertEquals("message 1 wrong", messages.get(0), readMessages.get(2) + "\n");
        Assert.assertEquals("message 1 wrong", messages.get(1), readMessages.get(3));
    }

    private static class TestStructure implements ITestStructure {
        public String testData;
    }

}
