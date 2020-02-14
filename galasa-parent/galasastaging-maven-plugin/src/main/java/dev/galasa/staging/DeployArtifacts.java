package dev.galasa.staging;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Generate scripts to deploy all the runtime artifacts to a new repository
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "deployartifacts", 
requiresProject = true,
requiresDependencyCollection = ResolutionScope.COMPILE, 
requiresDependencyResolution = ResolutionScope.COMPILE)
public class DeployArtifacts extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File         outputDirectory;
    
    @Parameter(defaultValue = "${galasa.target.repo}", property = "targetRepo", required = true)
    private URL               targetRepo;

    @Parameter(defaultValue = "${galasa.target.repo.id}", property = "targetRepoId", required = true)
    private String            targetRepoId;

    @Parameter(defaultValue = "${galasa.settings.file}", property = "settingsFile", required = false)
    private String            settingsFile;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        Path outputRepositoryDirectory = Paths.get(outputDirectory.toURI());
        Path scriptFile = outputRepositoryDirectory.resolve("deploy.sh");

        if (Files.exists(scriptFile)) {
            try {
                Files.delete(scriptFile);
            } catch (IOException e) {
                throw new MojoExecutionException("Problem deleting script file", e);
            }
        }

        HashMap<String, Artifact> artifacts = new HashMap<>();
        //*** process all artifacts with scope compile and built a list of all dependencies
        for (Artifact artifact : this.project.getArtifacts()) {
            if (!"dev.galasa".equals(artifact.getGroupId())) { //*** Only interested in galasa artifacts
                continue;
            }

            String fullId = artifact.toString();

            if (!artifacts.containsKey(fullId)) {
                artifacts.put(fullId, artifact);
            }
        }

        //*** Sort artifacts
        ArrayList<Artifact> sortedArtifacts = new ArrayList<>(artifacts.values());
        Collections.sort(sortedArtifacts);

        //*** Generate script
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash -e -x\n\n");

        for(Artifact artifact : sortedArtifacts) {
            System.out.println(artifact);

            //*** Copy file out of the local repository
            Path sourceFile = Paths.get(artifact.getFile().toURI());
            Path targetFile = outputRepositoryDirectory.resolve(sourceFile.getFileName());
            try {
                if (Files.exists(targetFile)) {
                    Files.delete(targetFile);
                }
                Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to copy artifact " + artifact, e);
            }

            sb.append("mvn deploy:deploy-file ");
            if (settingsFile != null) {
                sb.append("--settings '");
                sb.append(this.settingsFile);
                sb.append("' ");
            }
            sb.append("-Durl=");
            sb.append(this.targetRepo.toString());
            sb.append("' ");
            sb.append("-DrepositoryId=");
            sb.append(this.targetRepoId);
            sb.append("' ");
            sb.append("-Dfile='");
            sb.append(targetFile.toAbsolutePath().toString());
            sb.append("' ");
            sb.append("-DgroupId=");
            sb.append(artifact.getGroupId());
            sb.append(" ");
            sb.append("-DartifactId=");
            sb.append(artifact.getArtifactId());
            sb.append(" ");
            sb.append("-Dversion=");
            sb.append(artifact.getVersion());
            sb.append(" ");
            sb.append("-Dpackaging=");
            sb.append(artifact.getType());
            sb.append(" ");
            if (artifact.hasClassifier()) {
                sb.append("-Dclassifier=");
                sb.append(artifact.getClassifier());
            }
            sb.append("\n");
        }

        System.out.println(sb.toString());

        try {
            Files.write(scriptFile, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write script", e);
        }

    }

}
