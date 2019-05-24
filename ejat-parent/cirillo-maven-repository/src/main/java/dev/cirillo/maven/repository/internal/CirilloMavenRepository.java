package dev.cirillo.maven.repository.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import dev.cirillo.maven.repository.IMavenRepository;

@Component	
public class CirilloMavenRepository implements IMavenRepository {
	
	private URL localRepository;
	private List<URL> remoteRepositories = new ArrayList<URL>();
	
	@Override
	public URL getLocalRepository() {
		return this.localRepository;
	}

	@Override
	public List<URL> getRemoteRepositories() {
		return this.remoteRepositories;
	}

	@Override
	public void setRepositories(URL localRepository, List<URL> remoteRepositories) {
		this.localRepository = localRepository;
		this.remoteRepositories.addAll(remoteRepositories);
	}

	@Override
	public void addRemoteRepository(URL remoteRepository) {
		this.remoteRepositories.add(remoteRepository);
	}	
}
