package dev.galasa.framework.docker.controller.pojo;

import java.util.List;

public class Container {

	public String Id;      // NOSONAR
	public List<String> Names;      // NOSONAR
	
	public String State;      // NOSONAR
	public String Status;      // NOSONAR
	
	public Labels Labels;      // NOSONAR
	
	public String Image;      // NOSONAR
		
	public List<String> Cmd;      // NOSONAR
	
	public HostConfig HostConfig;      // NOSONAR
	
}
