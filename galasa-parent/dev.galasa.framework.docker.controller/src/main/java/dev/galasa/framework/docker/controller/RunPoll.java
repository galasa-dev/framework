/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.docker.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import dev.galasa.framework.docker.controller.pojo.Container;
import dev.galasa.framework.docker.controller.pojo.HostConfig;
import dev.galasa.framework.docker.controller.pojo.Labels;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import io.prometheus.client.Counter;

public class RunPoll implements Runnable {
	private final Log logger = LogFactory.getLog(getClass());

	private final Settings settings;
	private final CloseableHttpClient httpClient;
	private final IDynamicStatusStoreService dss;
	private final IFrameworkRuns runs;
	private final QueuedComparator queuedComparator = new QueuedComparator();

	private Counter    submittedRuns;

	private static Gson gson     = new GsonBuilder().setPrettyPrinting().create();

	public RunPoll(IDynamicStatusStoreService dss, Settings settings, CloseableHttpClient httpClient, IFrameworkRuns runs) {
		this.settings   = settings;
		this.httpClient = httpClient;
		this.runs       = runs;
		this.dss        = dss;

		//*** Create metrics

		this.submittedRuns = Counter.build()
				.name("galasa_docker_controller_submitted_runs")
				.help("The number of runs submitted by the Docker controller")
				.register();

	}

	@Override
	public void run() {
		logger.info("Looking for new runs");

		try {
			//*** No we are not, get all the queued runs
			List<IRun> queuedRuns = this.runs.getQueuedRuns();
			//TODO filter by capability

			//*** Remove all the local runs
			Iterator<IRun> queuedRunsIterator = queuedRuns.iterator();
			while(queuedRunsIterator.hasNext()) {
				IRun run = queuedRunsIterator.next();
				if (run.isLocal()) {
					queuedRunsIterator.remove();
				}
			}


			if (queuedRuns.isEmpty()) {
				logger.info("There are no queued runs");
				return;
			}

			while(true) {
				//*** Check we are not at max engines 
				List<Container> pods = getContainers(this.httpClient, this.settings);
				filterActiveRuns(pods);
				logger.info("Active runs=" + pods.size() + ",max=" + settings.getMaxEngines());

				int currentActive = pods.size();
				if (currentActive >= settings.getMaxEngines()) {
					logger.info("Not looking for runs, currently at maximim engines (" + settings.getMaxEngines() + ")");
					return;
				}

				//				List<IRun> activeRuns = this.runs.getActiveRuns();

				//  TODO Create the group algorithim same as the galasa scheduler

				//*** Build pool lists
				//			HashMap<String, Pool> queuePools = getPools(queuedRuns);
				//			HashMap<String, Pool> activePools = getPools(activeRuns);

				//*** cheat for the moment
				Collections.sort(queuedRuns, queuedComparator);

				IRun selectedRun = queuedRuns.remove(0);

				startPod(selectedRun);

				if (!queuedRuns.isEmpty()) {
					Thread.sleep((long)settings.getRunPollRecheck() * 1000l);  //*** Slight delay to allow Docker to catch up
				} else {
					return;
				}
			}
		} catch(Exception e) {
			logger.error("Unable to poll for new runs",e);
		}

		return;
	}

	private void startPod(IRun run) {
		String runName = run.getName();
		String engineName = this.settings.getEngineLabel() + "-" + runName.toLowerCase();

		logger.info("Received run " + runName);

		try {
			//*** First attempt to allocate the run to this controller
			HashMap<String, String> props = new HashMap<>();
			props.put("run." + runName + ".controller", settings.getPodName());
			if (!this.dss.putSwap("run." + runName + ".status", "queued", "allocated", props)) {
				logger.info("run allocated by another controller");
				return;
			}

			Container container = new Container();
			container.Labels = new Labels();
			container.Labels.galasaEngineController = this.settings.getEngineLabel();
			container.Labels.galasaRun              = runName;

			container.Image = settings.getEngineImage();

			container.HostConfig = new HostConfig();
			if (!settings.getNetwork().isEmpty()) {
				container.HostConfig.NetworkMode = settings.getNetwork();
			}

			if (!settings.getDns().isEmpty()) {
				container.HostConfig.Dns = new ArrayList<>(settings.getDns());
			}

			container.Cmd = new ArrayList<>();
			container.Cmd.add("java");
			container.Cmd.add("-jar");
			container.Cmd.add("boot.jar");
			container.Cmd.add("--obr");
			container.Cmd.add("file:galasa.obr");
			container.Cmd.add("--bootstrap");
			container.Cmd.add(settings.getBootstrap());
			container.Cmd.add("--run");
			container.Cmd.add(runName);
			if (run.isTrace()) {
				container.Cmd.add("--trace");
			}


			boolean successful = false;
			int retry = 0;
			String choosenEngineName = engineName;
			String id = null;
			while(!successful) {
				try {
					HttpPost post = new HttpPost(settings.getDockerUrl().toString() + "/containers/create?name=" + choosenEngineName);
					post.setEntity(new StringEntity(gson.toJson(container), ContentType.APPLICATION_JSON));					
					try (CloseableHttpResponse response = httpClient.execute(post)) {
						StatusLine status = response.getStatusLine();
						String entity = EntityUtils.toString(response.getEntity());

						if (status.getStatusCode() == HttpStatus.SC_CONFLICT) {
							retry++;
							choosenEngineName = engineName + "-" + retry;
							logger.info("Engine Pod " + engineName + " already exists, trying with " + choosenEngineName);
							continue;
						}

						if (status.getStatusCode() != HttpStatus.SC_CREATED) {
							throw new DockerControllerException("Container Create failed - " + status + "\n" + entity);
						}

						Container newContainer = gson.fromJson(entity, Container.class);
						id = newContainer.Id;
					}

					logger.info("Engine Container " + engineName + " created with id " + id);
					successful = true;
					break;
				} catch(Exception e) {
					logger.error("Failed to create engine container",e);
				}
				logger.info("Waiting 2 seconds before trying to create container again");
				Thread.sleep(2000);
			}

			//*** Now start the container

			successful = false;
			while(!successful) {
				try {
					HttpPost post = new HttpPost(settings.getDockerUrl().toString() + "/containers/" + id + "/start");
					try (CloseableHttpResponse response = httpClient.execute(post)) {
						StatusLine status = response.getStatusLine();
						EntityUtils.consume(response.getEntity());

						if (status.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
							throw new DockerControllerException("Container Start failed - " + status);
						}
					}

					logger.info("Engine Container " + engineName + " started with id " + id);
					successful = true;
					submittedRuns.inc();
					break;
				} catch(Exception e) {
					logger.error("Failed to start engine container",e);
				}
				logger.info("Waiting 2 seconds before trying to start container again");
				Thread.sleep(2000);
			}
		} catch(Exception e) {
			logger.error("Failed to start new engine",e);
		}
		return;
	}




	//	private HashMap<String, Pool> getPools(@NotNull List<IRun> runs) {
	//		HashMap<String, Pool> pools = new HashMap<>();
	//
	//		for(IRun run : runs) {
	//			String poolid = getPoolId(run);
	//			Pool pool = pools.get(poolid);
	//			if (pool == null) {
	//				pool = new Pool(poolid);
	//			}
	//			pool.runs.add(run);
	//		}
	//
	//		return pools;
	//	}
	//
	//	private String getPoolId(IRun run) {
	//		if (settings.getRequestorsByGroup().contains(run.getRequestor())) {
	//			return run.getRequestor() + "/" + run.getGroup(); 
	//		}
	//
	//		return run.getRequestor();
	//	}



	public static @NotNull List<Container> getContainers(CloseableHttpClient httpClient, Settings settings) throws DockerControllerException {
		LinkedList<Container> returnContainers = new LinkedList<>();

		try {
			HttpGet get = new HttpGet(settings.getDockerUrl().toString() + "/containers/json?all=true"); 
			try (CloseableHttpResponse response = httpClient.execute(get)) {
				StatusLine status = response.getStatusLine();
				String entity = EntityUtils.toString(response.getEntity());

				if (status.getStatusCode() != HttpStatus.SC_OK) {
					throw new DockerControllerException("Get containers failed - " + status);
				}

				JsonArray array = gson.fromJson(entity, JsonArray.class);
				for(JsonElement element : array) {
					Container container = gson.fromJson(element, Container.class);

					if (container.Labels == null) {
						continue;
					}

					if (container.Labels.galasaEngineController == null || container.Labels.galasaRun == null) {
						continue;
					}

					returnContainers.add(container);
				}
			}
		} catch(DockerControllerException e) {
			throw e;
		} catch(Exception e) {
			throw new DockerControllerException("Failed retrieving containers",e);
		}

		return returnContainers;
	}

	public static void filterActiveRuns(@NotNull List<Container> containers) {
		Iterator<Container> iContainer = containers.iterator();
		while(iContainer.hasNext()) {
			Container container = iContainer.next();
			if ("exited".equals(container.State)) {
				iContainer.remove();
			}
		}
	}

	public static void filterTerminated(@NotNull List<Container> containers) {
		Iterator<Container> iContainer = containers.iterator();
		while(iContainer.hasNext()) {
			Container container = iContainer.next();

			if ("exited".equals(container.State)) {
				continue;
			}
			iContainer.remove();
		}
	}


	//	private static class Pool implements Comparable<Pool> {
	//		private String id;
	//		private ArrayList<IRun> runs = new ArrayList<>();
	//
	//		public Pool(String id) {
	//			this.id = id;
	//		}
	//
	//		@Override
	//		public int compareTo(Pool o) {
	//			return runs.size() - o.runs.size();
	//		}	
	//
	//	}
	//
	//
	private static class QueuedComparator implements Comparator<IRun> {

		@Override
		public int compare(IRun o1, IRun o2) {
			return o1.getQueued().compareTo(o2.getQueued());
		}

	}


}
