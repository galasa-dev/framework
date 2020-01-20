package test.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

import dev.galasa.framework.gendocs.ManagerDoclet;

/**
 * Test the Doclet manager document extraction.
 * 
 * These tests do not test the actual contents of the markdown files,  would be a pain to change the test everytime
 * the templates were tweaked.
 * 
 * 
 * @author Michael Baylis
 *
 */
public class DocletTest {
    
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
     * Check the package name extraction utility method
     */
    @Test
    public void packageNameTest() {
        
        String className = "this.is.a.dummy.package.Test";
        String result = ManagerDoclet.getPackageName(className);
        assertThat(result).isEqualTo("this.is.a.dummy.package");
        
        className = "NoPackage";
        result = ManagerDoclet.getPackageName(className);
        assertThat(result).isEqualTo("");
               
    }
    
    
    /**
     * Confirm literals and links are converted
     */
    @Test
    public void stringConversionTest() {
        
        //*** test removal of new lines and @ literals
        String testString = "There is a possibility \nthat a women will {@literal @} land on mars in this decade.";
        String result = ManagerDoclet.getString(testString, "");
        assertThat(result).isEqualTo("There is a possibility that a women will @ land on mars in this decade.");
        
        //*** test links with classname
        
        testString = "Here is more doc {@link Spider} hopefully";
        result = ManagerDoclet.getString(testString, "a.package");
        assertThat(result).isEqualTo("Here is more doc [Spider](https://javadoc-snapshot.galasa.dev/a/package/Spider.html) hopefully");
        
        //test with full classname
        testString = "Here is more doc {@link a.Spider} hopefully";
        result = ManagerDoclet.getString(testString, "a.package");
        assertThat(result).isEqualTo("Here is more doc [a.Spider](https://javadoc-snapshot.galasa.dev/a/Spider.html) hopefully");       
    }
    
    /**
     * Test the extraction of the tags
     */
    @Test
    public void testTags() {
        //Test extraction of the tags from the Doc classes
        
        //Test null
        Tag tag = null;
        String result = ManagerDoclet.getTagString(tag, null);
        assertThat(result).isNull();
        
        // Test null tag text
        tag = mock(Tag.class);
        when(tag.text()).thenReturn(null);
        result = ManagerDoclet.getTagString(tag, null);
        assertThat(result).isNull();
        
        // Test empty tag text
        tag = mock(Tag.class);
        when(tag.text()).thenReturn("");
        result = ManagerDoclet.getTagString(tag, null);
        assertThat(result).isNull();
        
        
        // Test actual text
        tag = mock(Tag.class);
        when(tag.text()).thenReturn("this is proper stuff");
        result = ManagerDoclet.getTagString(tag, null);
        assertThat(result).isEqualTo("this is proper stuff");
        
        
        
        //test null tags and empty tags
        Tag[] tags = null;
        result = ManagerDoclet.getFirstTagString(tags, "");
        assertThat(result).isNull();
        
        tags = new Tag[0];
        result = ManagerDoclet.getFirstTagString(tags, "");
        assertThat(result).isNull();
        
        //***Test single empty tag
        tags = new Tag[1];
        tags[0] = mock(Tag.class);
        when(tags[0].text()).thenReturn("");
        result = ManagerDoclet.getFirstTagString(tags, "");
        assertThat(result).isNull();
        
        //***Test extraction of tags from array
        tags = new Tag[2];
        tags[0] = mock(Tag.class);
        tags[1] = mock(Tag.class);
        when(tags[0].text()).thenReturn("");
        when(tags[1].text()).thenReturn("this is proper stuff");
        result = ManagerDoclet.getFirstTagString(tags, "");
        assertThat(result).isEqualTo("this is proper stuff");
        
        // Test named tag
        tags = new Tag[1];
        tags[0] = mock(Tag.class);
        when(tags[0].text()).thenReturn("a valid result");
        
        Doc doc = mock(Doc.class);
        when(doc.tags("atag")).thenReturn(tags);
        
        result = ManagerDoclet.getTagString(doc, "atag", "a valid result");
        assertThat(result).isEqualTo("a valid result");
       
        // Test first sentence
        tags = new Tag[1];
        tags[0] = mock(Tag.class);
        when(tags[0].text()).thenReturn("a valid result");
        
        doc = mock(Doc.class);
        when(doc.firstSentenceTags()).thenReturn(tags);
        
        result = ManagerDoclet.getFirstSentenceString(doc, "a valid result");
        assertThat(result).isEqualTo("a valid result");
    }
    
    
    /**
     * Test the appropriate manager document files are created,  does not test the contents as that will be a right pain
     * 
     * @throws Exception if there are io problems
     */
    @Test
    public void testManagerFilesCreation() throws Exception {
        
        Tag descriptionTag = mock(Tag.class);
        when(descriptionTag.name()).thenReturn("@galasa.description");
        when(descriptionTag.text()).thenReturn("This is a description");
        
        
        Tag managerTag = mock(Tag.class);
        when(managerTag.name()).thenReturn("@galasa.manager");
        when(managerTag.text()).thenReturn("TestManager");     
        
        PackageDoc managerDoc = mock(PackageDoc.class);
        when(managerDoc.name()).thenReturn("com.test.package");
        when(managerDoc.tags()).thenReturn(new Tag[] {managerTag, descriptionTag});
        when(managerDoc.tags("@galasa.manager")).thenReturn(new Tag[] {managerTag});
        
        
        PackageDoc[] packageDocs = new PackageDoc[] {managerDoc};
        ClassDoc[] classDocs = new ClassDoc[0];
        
        RootDoc rootDoc = mock(RootDoc.class);
        when(rootDoc.specifiedPackages()).thenReturn(packageDocs);
        when(rootDoc.classes()).thenReturn(classDocs);
        
        ManagerDoclet.processRoot(rootDoc, this.testDirectory);
        
        Path testManagerDirectory = this.testDirectory.resolve("testmanager");
        Path testManagerId = testManagerDirectory.resolve("id.txt");
        Path testManagerMd = testManagerDirectory.resolve("manager.md");
        
        assertThat(Files.exists(testManagerDirectory)).as("manager directory exists").isTrue();
        assertThat(Files.isDirectory(testManagerDirectory)).as("manager directory is a directory").isTrue();
        assertThat(Files.exists(testManagerId)).as("manager id file exists").isTrue();
        assertThat(Files.exists(testManagerMd)).as("manager md exists").isTrue();
        
    }
    
    
    /**
     * Test the appropriate annotation document files are created,  does not test the contents as that will be a right pain
     * 
     * @throws Exception if there are io problems
     */
    @Test
    public void testAnnotationFileCreation() throws Exception {
        
        Tag descriptionTag = mock(Tag.class);
        when(descriptionTag.name()).thenReturn("@galasa.description");
        when(descriptionTag.text()).thenReturn("This is a description");
        
        
        Tag annotationTag = mock(Tag.class);
        when(annotationTag.name()).thenReturn("@galasa.annotation");
        
        ClassDoc annotationDoc = mock(ClassDoc.class);
        when(annotationDoc.qualifiedName()).thenReturn("com.test.package.TestAnnotation");
        when(annotationDoc.tags()).thenReturn(new Tag[] {annotationTag, descriptionTag});
        
        
        PackageDoc[] packageDocs = new PackageDoc[0];
        ClassDoc[] classDocs = new ClassDoc[] {annotationDoc};
        
        RootDoc rootDoc = mock(RootDoc.class);
        when(rootDoc.specifiedPackages()).thenReturn(packageDocs);
        when(rootDoc.classes()).thenReturn(classDocs);
        
        ManagerDoclet.processRoot(rootDoc, this.testDirectory);
        
        Path testAnnotationfile = this.testDirectory.resolve("annotation_com.test.package.TestAnnotation.md");
        
        assertThat(Files.exists(testAnnotationfile)).as("annotation file exists").isTrue();
    }
    
    
    /**
     * Test the appropriate cps document files are created,  does not test the contents as that will be a right pain
     * 
     * @throws Exception if there are io problems
     */
    @Test
    public void testCpsFileCreation() throws Exception {
        
        Tag descriptionTag = mock(Tag.class);
        when(descriptionTag.name()).thenReturn("@galasa.description");
        when(descriptionTag.text()).thenReturn("This is a description");
        
        
        Tag cpsTag = mock(Tag.class);
        when(cpsTag.name()).thenReturn("@galasa.cps.property");
        
        ClassDoc cpsDoc = mock(ClassDoc.class);
        when(cpsDoc.qualifiedName()).thenReturn("com.test.package.TestCps");
        when(cpsDoc.tags()).thenReturn(new Tag[] {cpsTag, descriptionTag});
        
        
        PackageDoc[] packageDocs = new PackageDoc[0];
        ClassDoc[] classDocs = new ClassDoc[] {cpsDoc};
        
        RootDoc rootDoc = mock(RootDoc.class);
        when(rootDoc.specifiedPackages()).thenReturn(packageDocs);
        when(rootDoc.classes()).thenReturn(classDocs);
        
        ManagerDoclet.processRoot(rootDoc, this.testDirectory);
        
        Path testCpsfile = this.testDirectory.resolve("cps_com.test.package.TestCps.md");
        
        assertThat(Files.exists(testCpsfile)).as("cps file exists").isTrue();
    }
    
    
}