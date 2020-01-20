/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package test.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.gendocs.BuildManagerDoc;

public class ManagerDocTest {
    
    public Path testDirectory;
    
    /**
     * Create a temporary directory for the tests to store the results in
     * 
     * @throws IOException if a problem with the temp directory
     */
    @Before
    public void createTemporaryDirectory() throws IOException {
        this.testDirectory = Files.createTempDirectory("testmanagerdocs");
    }
    
    
    /**
     * Clean up
     * 
     * @throws IOException if problem deleting temporary directory
     */
    @After
    public void deleteTemporaryDirectory() throws IOException {
        FileUtils.deleteDirectory(this.testDirectory.toFile());
    }
    
    /**
     * Test that all the generated files are found and included in the final markdown file.
     * it does not test the formatting of the markdown file from the template as this would 
     * be a pain to have to change the test for every template tweak.
     * 
     * 
     * @throws MojoExecutionException should not happen
     * @throws MojoFailureException should not happen
     * @throws IOException should not happen
     */
    @Test
    public void MergeTest() throws MojoExecutionException, MojoFailureException, IOException {
        Path src = this.testDirectory.resolve("src");
        Path res = this.testDirectory.resolve("res");
        
        MavenProject project = new MavenProject();
        project.setPackaging("bundle");
        project.addCompileSourceRoot(src.toAbsolutePath().toString());
        
        Resource resource = new Resource();
        resource.setDirectory(res.toAbsolutePath().toString());
        project.addResource(resource);
        
        BuildManagerDoc buildManagerDoc = new BuildManagerDoc();
        buildManagerDoc.project = project;
        buildManagerDoc.outputDirectory = testDirectory.toFile();
        buildManagerDoc.managerDocDir = testDirectory.toFile();

        
        Path apiDocs = this.testDirectory.resolve("managerdocs").resolve("apidocs");     
        Path managerDir = apiDocs.resolve("manager");
        Files.createDirectories(managerDir);
        
        Files.write(managerDir.resolve("id.txt"), "TestManager".getBytes());
        Files.write(managerDir.resolve("manager.md"), "Test Manager markdown test".getBytes());
        Files.write(managerDir.resolve("annotation_test1.md"), "Test Annotation 1 markdown test".getBytes());
        Files.write(managerDir.resolve("cps_test1.md"), "Test CPS 1 markdown test".getBytes());
        
        Files.write(apiDocs.resolve("annotation_test2.md"), "Test Annotation 2 markdown test".getBytes());
        Files.write(apiDocs.resolve("cps_test2.md"), "Test CPS 2 markdown test".getBytes());
        
        Files.createDirectories(src);
        Files.createDirectories(res);
        Files.write(src.resolve("codesnippet_test1.md"), "Test code 1 markdown test".getBytes());
        Files.write(res.resolve("codesnippet_test2.md"), "Test code 2 markdown test".getBytes());
              
        
        buildManagerDoc.execute();
        
        Path md = this.testDirectory.resolve("testmanager-manager.md");
        assertThat(Files.exists(md)).as("md generated").isTrue();
      
        String markdown = new String(Files.readAllBytes(md));
        
        assertThat(markdown).contains("TestManager Manager");
        assertThat(markdown).contains("Test Manager markdown test");
        assertThat(markdown).contains("Test Annotation 1 markdown test");
        assertThat(markdown).contains("Test Annotation 2 markdown test");
        assertThat(markdown).contains("Test CPS 1 markdown test");
        assertThat(markdown).contains("Test CPS 2 markdown test");
        assertThat(markdown).contains("Test code 1 markdown test");
        assertThat(markdown).contains("Test code 2 markdown test");
        
    }


}
