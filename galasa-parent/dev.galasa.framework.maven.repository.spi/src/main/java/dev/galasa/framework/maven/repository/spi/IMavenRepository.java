/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.maven.repository.spi;

import java.net.URL;
import java.util.List;

public interface IMavenRepository {
	
	URL getLocalRepository();
	
	List<URL> getRemoteRepositories();
	
	void setRepositories(URL localRepository, List<URL> remoteRepositories);
	
	void addRemoteRepository(URL remoteRepository);

}
