package io.ejat.framework.internal.resourcemanagement.runs;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IResourceManagement;
import io.ejat.framework.spi.IResourceManagementProvider;
import io.ejat.framework.spi.ResourceManagerException;

@Component(service= {IResourceManagementProvider.class})
public class RunResourceManagement implements IResourceManagementProvider {
	
	private IFramework                         framework;
	private IResourceManagement                resourceManagement;
	private IDynamicStatusStoreService         dss;
	private IConfigurationPropertyStoreService cps;
	
	@Override
	public boolean initialise(IFramework framework, IResourceManagement resourceManagement) throws ResourceManagerException {
		this.framework = framework;
		this.resourceManagement = resourceManagement;
		try {
			this.dss = this.framework.getDynamicStatusStoreService("framework");
			this.cps = this.framework.getConfigurationPropertyService("framework");
		} catch (Exception e) {
			throw new ResourceManagerException("Unable to initialise Active Run resource monitor", e);
		}
		
		return true;
	}

	@Override
	public void start() {
		this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(new RunDeadHeartbeatMonitor(this.framework, this.resourceManagement, this.dss, this,cps), 
				this.framework.getRandom().nextInt(20), 
				20, 
				TimeUnit.SECONDS);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void runFinishedOrDeleted(String runName) {
	}

}
