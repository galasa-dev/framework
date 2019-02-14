package io.ejat.maven.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.apache.felix.bundlerepository.impl.ResourceImpl;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

@Mojo(name = "obrresources", 
	  defaultPhase = LifecyclePhase.PROCESS_RESOURCES , 
	  threadSafe = true,
	  requiresDependencyCollection = ResolutionScope.COMPILE,
	  requiresDependencyResolution = ResolutionScope.COMPILE)
public class BuildOBRResources extends AbstractMojo
{

	@Parameter( defaultValue = "${project}", readonly = true )
	private MavenProject project;

	@Component
	private MavenProjectHelper projectHelper;

	@Parameter( defaultValue = "${basedir}", property = "basedir", required = true )
	private File baseDirectory;

	@Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
	private File outputDirectory;

	private RepositoryImpl oldRepository = null;
	private RepositoryImpl newRepository = null;
	private DataModelHelper obrDataModelHelper = null;
	private HashMap<String, Resource> oldResources = new HashMap<String, Resource>();

	private File oldRepositoryFile = null;

	private boolean updated = false;

	public void execute() throws MojoExecutionException, MojoFailureException {

		obrDataModelHelper = new DataModelHelperImpl();

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		oldRepositoryFile = new File(outputDirectory, "repository.obr");
		project.getArtifact().setFile(oldRepositoryFile);

		if (oldRepositoryFile.exists()) {
			try {
				FileReader fr = new FileReader(oldRepositoryFile);
				oldRepository = (RepositoryImpl) obrDataModelHelper.readRepository(fr);
				fr.close();
			} catch(Exception e) {
				oldRepository = null;
				getLog().warn("BuildOBRResources: Unable to read old obr, creating a new one", e);
			}
		}

		if (oldRepository == null) {
			oldRepository = new RepositoryImpl();
			updated = true;
		}
		newRepository = new RepositoryImpl();


		for(Resource oldResource : oldRepository.getResources()) {
			oldResources.put(oldResource.getId(), oldResource);
		}

		for(Object dependency : project.getDependencyArtifacts()) {
			if (dependency instanceof DefaultArtifact) {
				DefaultArtifact artifact = (DefaultArtifact)dependency;
				if (artifact.isResolved() && artifact.getFile().getName().endsWith(".jar") && artifact.getScope().equals("compile")) {
					processBundle(artifact);
				}
			}
		}


		if (newRepository.getResources() == null || newRepository.getResources().length == 0) {
			throw new MojoFailureException("No resources have been added to the repository");
		}


		if (!oldResources.isEmpty()) {
			updated = true;
		}
		
		getLog().info("BuildOBRResources: OBR Artifact ID is " + project.getArtifact().getId());

		if (!updated) {
			getLog().info("BuildOBRResources: OBR is up to date");
			return;
		}

		newRepository.setName(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		newRepository.setLastModified(sdf.format(Calendar.getInstance().getTime()));

		try {
			FileWriter fw = new FileWriter(oldRepositoryFile);
			obrDataModelHelper.writeRepository(newRepository, fw);
			fw.close();
		} catch(Exception e) {
			throw new MojoExecutionException("Problem with writing repository.xml", e);
		}

		if (newRepository.getResources().length == 1) {
			getLog().info("BuildOBRResources: Repository created with " + newRepository.getResources().length + " resource");
		} else {
			getLog().info("BuildOBRResources: Repository created with " + newRepository.getResources().length + " resources");
		} 

		return;
	}

	private void processBundle(DefaultArtifact artifact) {

		try {
			ResourceImpl newResource = (ResourceImpl)obrDataModelHelper.createResource(artifact.getFile().toURI().toURL());

			URL name = artifact.getFile().toURI().toURL();
			newResource.put(Resource.URI, name);

			
			Resource oldResource = oldResources.remove(newResource.getId());
			if (oldResource == null) {
				updated = true;
			} 

			newRepository.addResource(newResource);
			
			getLog().info("BuildOBRResources: Added bundle " + newResource.getPresentationName() + " - " + newResource.getId() + " to repository");
		} catch (Exception e) {
			getLog().warn("BuildOBRResources: Failed to process dependency " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion());
		}

		return;
	}

}
