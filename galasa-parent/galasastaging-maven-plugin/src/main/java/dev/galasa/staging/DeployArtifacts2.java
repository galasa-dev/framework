/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.staging;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * Generate scripts to deploy all the runtime artifacts to a new repository
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "deployartifacts2", 
requiresProject = true,
requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, 
requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DeployArtifacts2 extends AbstractMojo {

    @Component
    private RepositorySystem        repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    private List<RemoteRepository> projectRepos;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${galasa.target.repo}", property = "targetRepo", required = true)
    private URL               targetRepo;

    @Parameter(defaultValue = "${galasa.target.repo.id}", property = "targetRepoId", required = false)
    private String            targetRepoId;

    public void execute() throws MojoExecutionException, MojoFailureException {

        HashMap<String, Artifact> artifacts = new HashMap<>();
        //*** process direct dependency artifacts with scope compile
        for (Artifact artifact : this.project.getDependencyArtifacts()) {
            if (!"compile".equals(artifact.getScope())) {
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

        ArrayList<org.eclipse.aether.artifact.Artifact> deployArtifacts = new ArrayList<>();

        for(Artifact artifact : sortedArtifacts) {
            System.out.println(artifact);   

            if ("pom".equals(artifact.getType())) {
                // Simply add poms to the deploy list
                deployArtifacts.add(convertArtifact(artifact));
            } else if ("obr".equals(artifact.getType())) {
                // Simply add obrs to the deploy list
                deployArtifacts.add(convertArtifact(artifact));
            } else if ("jar".equals(artifact.getType())) {
                processJar(artifact, deployArtifacts);
            }
        }

        // Now deploy them all
        Builder builder = new RemoteRepository.Builder(null, "default", targetRepo.toString());
        RemoteRepository distRepository = builder.build();

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setArtifacts(deployArtifacts);
        deployRequest.setRepository(distRepository);

        try {
            this.repoSystem.deploy(repoSession, deployRequest);
        } catch (DeploymentException e) {
            throw new MojoExecutionException("Unable to deploy artifacts", e);
        }
    }

    private void processJar(Artifact jarArtifact, ArrayList<org.eclipse.aether.artifact.Artifact> deployArtifacts) {
        // First add the JAR to the deployable artifacts
        deployArtifacts.add(convertArtifact(jarArtifact));

        // Get a hold of the POM
        org.eclipse.aether.artifact.Artifact pomArtifact = resolveArtifact(jarArtifact, null, "pom");
        if (pomArtifact != null) {
            deployArtifacts.add(convertArtifact(pomArtifact));
        }

        // Get a hold of the MODULE
        org.eclipse.aether.artifact.Artifact moduleArtifact = resolveArtifact(jarArtifact, null, "module");
        if (moduleArtifact != null) {
            deployArtifacts.add(convertArtifact(moduleArtifact));
        }

        // Get a hold of the SOURCES
        org.eclipse.aether.artifact.Artifact sourcesArtifact = resolveArtifact(jarArtifact, "sources", "jar");
        if (sourcesArtifact != null) {
            deployArtifacts.add(convertArtifact(sourcesArtifact));
        }

        // Get a hold of the JAVADOC
        org.eclipse.aether.artifact.Artifact javadocArtifact = resolveArtifact(jarArtifact, "javadoc", "jar");
        if (javadocArtifact != null) {
            deployArtifacts.add(convertArtifact(javadocArtifact));
        }

    }

    private org.eclipse.aether.artifact.Artifact resolveArtifact(Artifact artifact, String classifier, String extension) {

        DefaultArtifact extractArtifact = new DefaultArtifact(artifact.getGroupId(),
                artifact.getArtifactId(), classifier, extension, artifact.getBaseVersion());

        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(extractArtifact);
        request.setRepositories(projectRepos);

        ArtifactResult result = null;
        try {
            result = repoSystem.resolveArtifact(repoSession, request);
        } catch (Exception e) {
            getLog().warn(e.getMessage());
            return null;
        }

        return result.getArtifact();
    }


    private org.eclipse.aether.artifact.Artifact convertArtifact(org.eclipse.aether.artifact.Artifact artifact) {
        DefaultArtifact defaultArtifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), artifact.getExtension(), artifact.getBaseVersion());
        org.eclipse.aether.artifact.Artifact newArtifact = defaultArtifact.setFile(artifact.getFile());
        return newArtifact;
    }

    private org.eclipse.aether.artifact.Artifact convertArtifact(Artifact artifact) {
        DefaultArtifact defaultArtifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), artifact.getType(), artifact.getBaseVersion());
        org.eclipse.aether.artifact.Artifact newArtifact = defaultArtifact.setFile(artifact.getFile());
        return newArtifact;
    }

}
