package dev.galasa.framework.gendocs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * this goal will sxtract the generated markdown files from the java doc and build a manager.md file in the 
 * intended git repo directory.
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "buildmanagerdoc", 
requiresProject = true)
public class BuildManagerDoc extends AbstractMojo {
    
    private static VelocityEngine ve = new VelocityEngine();
    {
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    }

    @Parameter(defaultValue = "${project}", readonly = true)
    public MavenProject            project;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    public File                    outputDirectory;

    @Parameter(defaultValue = "${galasa.manager.doc.directory}", property = "managerDocDir", required = true)
    public File                    managerDocDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        //*** Make sure it is a bundle,  all managers are bundles
        if (!"bundle".equals(project.getPackaging())) {
            return;
        }

        try {
            //*** check that have a managerdoc directory
            Path managerDocPath = Paths.get(this.outputDirectory.toURI()).resolve("managerdocs").resolve("apidocs");
            if (!Files.exists(managerDocPath)) {
                return;
            }

            //*** Process the managers that exist in the bundle
            try (Stream<Path> paths = Files.list(managerDocPath)) {
                paths.forEach(new Consumer<Path>() {

                    @Override
                    public void accept(Path t) {
                        System.out.println(t);
                        if (Files.isDirectory(t)) {
                            processManagerDirectory(t);
                        }
                    }
                });
            }

        } catch(Exception e) {
            throw new MojoExecutionException("Problem processing the Manager Documentation files", e);
        }


    }

    private void processManagerDirectory(Path managerDirectory) {
        try {
            Path idFile = managerDirectory.resolve("id.txt");
            Path managerFile = managerDirectory.resolve("manager.md");

            if (!Files.exists(idFile) | !Files.exists(managerFile)) {
                return;
            }

            String id = new String(Files.readAllBytes(idFile));
            String manager = new String(Files.readAllBytes(managerFile));

            getLog().info("Processing documents for Manager " + id);
            
            
            ArrayList<Path> annotationFiles = new ArrayList<Path>();
            ArrayList<Path> codeSnippetFiles = new ArrayList<Path>();
            ArrayList<Path> cpsPropertyFiles = new ArrayList<Path>();
            
            
            //*** Search the manager directory for manager specific files
            try (Stream<Path> paths = Files.list(managerDirectory)) {
                paths.forEach(new Consumer<Path>() {

                    @Override
                    public void accept(Path t) {
                        if (!Files.isRegularFile(t)) {
                            return;
                        }
                        String fileName = t.getFileName().toString();
                        if (!fileName.endsWith(".md")) {
                            return;
                        }
                        
                        if (fileName.startsWith("cps_")) {
                            cpsPropertyFiles.add(t);
                        } else if (fileName.startsWith("annotation_")) {
                            annotationFiles.add(t);
                        }
                    }
                });
            }
            
            //*** Search the manager directory for common files in the bundle
            try (Stream<Path> paths = Files.list(managerDirectory.getParent())) {
                paths.forEach(new Consumer<Path>() {

                    @Override
                    public void accept(Path t) {
                        if (!Files.isRegularFile(t)) {
                            return;
                        }
                        String fileName = t.getFileName().toString();
                        if (!fileName.endsWith(".md")) {
                            return;
                        }
                        
                        if (fileName.startsWith("cps_")) {
                            cpsPropertyFiles.add(t);
                        } else if (fileName.startsWith("annotation_")) {
                            annotationFiles.add(t);
                        }
                    }
                });
            }
            
            
            //*** Search for code snippet files in the source directories
            
            for(String sourceRoot : this.project.getCompileSourceRoots()) {
                Path sourceRootPath = FileSystems.getDefault().getPath(sourceRoot);
                
                try (Stream<Path> paths = Files.walk(sourceRootPath.getParent())) {
                    paths.forEach(new Consumer<Path>() {

                        @Override
                        public void accept(Path t) {
                            if (!Files.isRegularFile(t)) {
                                return;
                            }
                            String fileName = t.getFileName().toString();
                            if (!fileName.endsWith(".md")) {
                                return;
                            }
                            
                            if (fileName.startsWith("codesnippet_")) {
                                codeSnippetFiles.add(t);
                            }
                        }
                    });
                }
            }
            
            for(Resource resource : this.project.getResources()) {
                Path resourceRootPath = FileSystems.getDefault().getPath(resource.getDirectory());
                
                try (Stream<Path> paths = Files.walk(resourceRootPath.getParent())) {
                    paths.forEach(new Consumer<Path>() {

                        @Override
                        public void accept(Path t) {
                            if (!Files.isRegularFile(t)) {
                                return;
                            }
                            String fileName = t.getFileName().toString();
                            if (!fileName.endsWith(".md")) {
                                return;
                            }
                            
                            if (fileName.startsWith("codesnippet_")) {
                                codeSnippetFiles.add(t);
                            }
                        }
                    });
                }
            }
            
            
            //*** sort then in filename order
            Collections.sort(annotationFiles, new FileNameComparator());
            Collections.sort(codeSnippetFiles, new FileNameComparator());
            Collections.sort(cpsPropertyFiles, new FileNameComparator());
            
            
            //*** read the contents of those files
            ArrayList<String> annotations = readFiles(annotationFiles);
            ArrayList<String> codeSnippets = readFiles(codeSnippetFiles);
            ArrayList<String> cpsProperties = readFiles(cpsPropertyFiles);
            
            
            String filename = id.toLowerCase().replaceAll("[\\s/\\\\]", "_");
            
            VelocityContext context = new VelocityContext();
            context.put("name", id);
            context.put("filename", filename);
            context.put("manager", manager);
            context.put("annotations", annotations);
            context.put("codeSnippets", codeSnippets);
            context.put("cpsProperties", cpsProperties);
            
            Template topicTemplate = ve.getTemplate("/managerTopic.template");
            
            Path managerOutputPath = Paths.get(managerDocDir.toURI()).resolve(filename + "-manager.md");
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((Files.newOutputStream(managerOutputPath))));
            topicTemplate.merge(context, writer);
            writer.close();
            
        } catch(IOException e) {
            getLog().error("Unable to process manager at " + managerDirectory.toString());
        }

    } 
    
    
    private ArrayList<String> readFiles(ArrayList<Path> files) throws IOException {
        if (files.isEmpty()) {
            return null;
        }
        
        ArrayList<String> contents = new ArrayList<String>();
        
        for(Path file : files) {
            contents.add(new String(Files.readAllBytes(file)));
        }
        
        return contents;
    }


    public static class FileNameComparator implements Comparator<Path> {

        @Override
        public int compare(Path o1, Path o2) {
            return o1.getFileName().toString().compareTo(o2.getFileName().toString());
        }
        
    }


}
