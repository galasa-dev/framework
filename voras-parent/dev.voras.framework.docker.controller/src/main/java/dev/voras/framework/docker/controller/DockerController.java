package dev.voras.framework.docker.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgi.service.component.annotations.Component;

import dev.voras.framework.FrameworkInitialisation;
import dev.voras.framework.spi.FrameworkException;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.IFramework;
import io.prometheus.client.exporter.HTTPServer;

@Component(service={DockerController.class})
public class DockerController {

	private Log logger = LogFactory.getLog(this.getClass());

	private boolean shutdown = false;
	private boolean shutdownComplete = false;

	private ScheduledExecutorService scheduledExecutorService;

	private HTTPServer metricsServer;

	private Health healthServer;

	public void run(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException  {

		//*** Add shutdown hook to allow for orderly shutdown
		Runtime.getRuntime().addShutdownHook(new ShutdownHook()); 

		//*** Initialise the framework services
		FrameworkInitialisation frameworkInitialisation = null;
		try {
			frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
		} catch (Exception e) {
			throw new FrameworkException("Unable to initialise the Framework Services", e);
		}
		IFramework framework = frameworkInitialisation.getFramework();

		IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");
		IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("framework");

		//*** Now start the Docker Controller framework

		logger.info("Starting Docker Controller");

		//*** Create the Http Client to Docker

		//*** Fetch the settings

		Settings settings;
		try {
			settings = new Settings();
		} catch (MalformedURLException e) {
			throw new DockerControllerException("Unable to initialise settings", e);
		}

		//*** Setup defaults and properties

		int metricsPort        = 9010;
		int healthPort         = 9011;

		String port = nulled(cps.getProperty("controller.metrics", "port"));
		if (port != null) {
			metricsPort = Integer.parseInt(port);
		}

		port = nulled(cps.getProperty("controller.health", "port"));
		if (port != null) {
			healthPort = Integer.parseInt(port);
		}

		//*** Setup scheduler
		scheduledExecutorService = new ScheduledThreadPoolExecutor(3);

		//*** Start the heartbeat
		scheduledExecutorService.scheduleWithFixedDelay(new Heartbeat(dss, settings), 
				0, 
				20, 
				TimeUnit.SECONDS);
		//*** Start the settings poll
		scheduledExecutorService.scheduleWithFixedDelay(settings, 
				20, 
				20, 
				TimeUnit.SECONDS);

		//*** Start the metrics server
		if (metricsPort > 0) {
			try {
				this.metricsServer = new HTTPServer(metricsPort);
				logger.info("Metrics server running on port " + metricsPort);
			} catch (IOException e) {
				throw new DockerControllerException("Unable to start the metrics server",e);
			}
		} else {
			logger.info("Metrics server disabled");
		}

		//*** Create metrics
		//		DefaultExports.initialize()  - problem within the the exporter at the moment TODO

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			//*** Create Health Server
			if (healthPort > 0) {		
				this.healthServer = new Health(healthPort);
				logger.info("Health monitoring on port " + healthPort);
			} else {
				logger.info("Health monitoring disabled");
			}
			//*** Start the run polling
			RunDeleted runDeleted = new RunDeleted(settings, httpClient, framework.getFrameworkRuns());
			scheduledExecutorService.scheduleWithFixedDelay(runDeleted, 
					0, 
					settings.getRunPoll(), 
					TimeUnit.SECONDS);
			RunPoll runPoll = new RunPoll(dss, settings, httpClient, framework.getFrameworkRuns());
			scheduledExecutorService.scheduleWithFixedDelay(runPoll, 
					1, 
					settings.getRunPoll(), 
					TimeUnit.SECONDS);
			
			logger.info("Docker controller has started");

			//*** Loop until we are asked to shutdown
			while(!shutdown) {
				try {
					Thread.sleep(500);
				} catch(Exception e) {
					throw new DockerControllerException("Interrupted sleep", e);
				}
			}

			//*** shutdown the scheduler
			this.scheduledExecutorService.shutdown();
			try {
				this.scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS);
			} catch(Exception e) {
				logger.error("Unable to shutdown the scheduler");
			}
		} catch(Exception e) {
			throw new DockerControllerException("Controller failed", e);
		}

		//*** Stop the metics server
		if (metricsPort > 0) {
			this.metricsServer.stop();
		}

		//*** Stop the health server
		if (healthPort > 0) {
			this.healthServer.shutdown();
		}

		logger.info("Docker Controller shutdown");
		shutdownComplete = true;
		return;

	}

	private class ShutdownHook extends Thread {
		@Override
		public void run() {
			DockerController.this.logger.info("Shutdown request received");
			DockerController.this.shutdown = true;

			while(!shutdownComplete) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					DockerController.this.logger.info("Shutdown wait was interrupted",e);
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}

	/**
	 * null a String is if it is empty
	 * 
	 * TODO Needs to be moved to a more appropriate place as non managers use this,  a stringutils maybe
	 * 
	 * @param value 
	 * @return a trimmed String or a null if emtpy or null
	 */
	public static String nulled(String value) {
		if (value == null) { 
			return null;
		}

		value = value.trim();
		if (value.isEmpty()) {
			return value;
		}
		return value;
	}


}
