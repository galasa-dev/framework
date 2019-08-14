package dev.voras.framework.k8s.controller;

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

import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.IFrameworkRuns;
import dev.voras.framework.spi.IRun;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Affinity;
import io.kubernetes.client.models.V1ConfigMapKeySelector;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1EnvVar;
import io.kubernetes.client.models.V1EnvVarSource;
import io.kubernetes.client.models.V1NodeAffinity;
import io.kubernetes.client.models.V1NodeSelectorRequirement;
import io.kubernetes.client.models.V1NodeSelectorTerm;
import io.kubernetes.client.models.V1ObjectFieldSelector;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodStatus;
import io.kubernetes.client.models.V1PreferredSchedulingTerm;
import io.kubernetes.client.models.V1ResourceRequirements;
import io.kubernetes.client.models.V1SecretKeySelector;
import io.prometheus.client.Counter;

public class RunPoll implements Runnable {
	private final Log logger = LogFactory.getLog(getClass());

	private final Settings settings;
	private final CoreV1Api api;
	private final IDynamicStatusStoreService dss;
	private final IFrameworkRuns runs;
	private final QueuedComparator queuedComparator = new QueuedComparator();

	private Counter    submittedRuns;

	public RunPoll(IDynamicStatusStoreService dss, Settings settings, CoreV1Api api, IFrameworkRuns runs) {
		this.settings  = settings;
		this.api       = api;
		this.runs      = runs;
		this.dss       = dss;

		//*** Create metrics

		this.submittedRuns = Counter.build()
				.name("cirillo_k8s_controller_submitted_runs")
				.help("The number of runs submitted by the Kubernetes controller")
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
				List<V1Pod> pods = getPods(this.api, this.settings);
				filterActiveRuns(pods);
				logger.info("Active runs=" + pods.size() + ",max=" + settings.getMaxEngines());

				int currentActive = pods.size();
				if (currentActive >= settings.getMaxEngines()) {
					logger.info("Not looking for runs, currently at maximim engines (" + settings.getMaxEngines() + ")");
					return;
				}

				//			List<IRun> activeRuns = this.runs.getActiveRuns();

				//  TODO Create the group algorithim same as the ejat scheduler

				//*** Build pool lists
				//			HashMap<String, Pool> queuePools = getPools(queuedRuns);
				//			HashMap<String, Pool> activePools = getPools(activeRuns);

				//*** cheat for the moment
				Collections.sort(queuedRuns, queuedComparator);

				IRun selectedRun = queuedRuns.remove(0);

				startPod(selectedRun);
				
				if (!queuedRuns.isEmpty()) {
					Thread.sleep(1000);  //*** Slight delay to allow Kubernetes to catch up
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
		String namespace = this.settings.getNamespace();

		logger.info("Received run " + runName);

		try {
			//*** First attempt to allocate the run to this controller
			HashMap<String, String> props = new HashMap<>();
			props.put("run." + runName + ".controller", settings.getPodName());
			if (!this.dss.putSwap("run." + runName + ".status", "queued", "allocated", props)) {
				logger.info("run allocated by another controller");
				return;
			}

			V1Pod newPod = new V1Pod();
			newPod.setApiVersion("v1");
			newPod.setKind("Pod");

			V1ObjectMeta metadata = new V1ObjectMeta();
			newPod.setMetadata(metadata);
			metadata.setName(engineName);
			metadata.putLabelsItem("cirillo-engine-controller", this.settings.getEngineLabel());
			metadata.putLabelsItem("cirillo-run", runName);

			V1PodSpec podSpec = new V1PodSpec();
			newPod.setSpec(podSpec);
			podSpec.setRestartPolicy("Never");

			String nodeArch = this.settings.getNodeArch();
			if (!nodeArch.isEmpty()) {
				HashMap<String, String> nodeSelector = new HashMap<>();
				nodeSelector.put("beta.kubernetes.io/arch", nodeArch);
				podSpec.setNodeSelector(nodeSelector);
			}

			String nodePreferredAffinity = this.settings.getNodePreferredAffinity();
			if (!nodePreferredAffinity.isEmpty()) {
				String[] selection = nodePreferredAffinity.split("=");
				if (selection.length == 2) {
					V1Affinity affinity = new V1Affinity();
					podSpec.setAffinity(affinity);

					V1NodeAffinity nodeAffinity = new V1NodeAffinity();
					affinity.setNodeAffinity(nodeAffinity);

					V1PreferredSchedulingTerm preferred = new V1PreferredSchedulingTerm();
					nodeAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(preferred);
					preferred.setWeight(1);

					V1NodeSelectorTerm selectorTerm = new V1NodeSelectorTerm();
					preferred.setPreference(selectorTerm);

					V1NodeSelectorRequirement requirement = new V1NodeSelectorRequirement();
					selectorTerm.addMatchExpressionsItem(requirement);
					requirement.setKey(selection[0]);
					requirement.setOperator("In");
					requirement.addValuesItem(selection[1]);
				}
			}

			V1Container container = new V1Container();
			podSpec.addContainersItem(container);
			container.setName("engine");
			container.setImage(this.settings.getEngineImage());
			container.setImagePullPolicy("Always"); // TODO parameterise

			ArrayList<String> commands = new ArrayList<>();
			container.setCommand(commands);
			commands.add("java");

			ArrayList<String> args = new ArrayList<>();
			container.setArgs(args);
			args.add("-jar");
			args.add("boot.jar");
			args.add("--obr");
			args.add("file:voras.obr");
			args.add("--bootstrap");
			args.add(settings.getBootstrap());
			args.add("--run");
			args.add(runName);
			if (run.isTrace()) {
				args.add("--trace");
			}


			V1ResourceRequirements resources = new V1ResourceRequirements();
			container.setResources(resources);

			// TODO reinstate
			//			System.out.println("requests=" + Integer.toString(this.settings.getEngineMemoryRequest()) + "Mi");
			//			System.out.println("limit=" + Integer.toString(this.settings.getEngineMemoryLimit()) + "Mi");
			//			resources.putRequestsItem("memory", new Quantity(Integer.toString(this.settings.getEngineMemoryRequest()) + "Mi"));
			//			resources.putLimitsItem("memory", new Quantity(Integer.toString(this.settings.getEngineMemoryLimit()) + "Mi"));

			ArrayList<V1EnvVar> envs = new ArrayList<>();
			container.setEnv(envs);
			//			envs.add(createConfigMapEnv("EJAT_URL", configMapName, "ejat_url"));
			//			envs.add(createConfigMapEnv("EJAT_INFRA_OBR", configMapName, "ejat_maven_infra_obr"));
			//			envs.add(createConfigMapEnv("EJAT_INFRA_REPO", configMapName, "ejat_maven_infra_repo"));
			//			envs.add(createConfigMapEnv("EJAT_TEST_REPO", configMapName, "ejat_maven_test_repo"));
			//			envs.add(createConfigMapEnv("EJAT_HELPER_REPO", configMapName, "ejat_maven_helper_repo"));
			//
			//			envs.add(createValueEnv("EJAT_ENGINE_TYPE", engineLabel));
			envs.add(createValueEnv("MAX_HEAP", Integer.toString(this.settings.getEngineMemory()) + "m"));
			//
			//			envs.add(createSecretEnv("EJAT_SERVER_USER", "ejat-secret", "ejat-server-username"));
			//			envs.add(createSecretEnv("EJAT_SERVER_PASSWORD", "ejat-secret", "ejat-server-password"));
			//			envs.add(createSecretEnv("EJAT_MAVEN_USER", "ejat-secret", "ejat-maven-username"));
			//			envs.add(createSecretEnv("EJAT_MAVEN_PASSWORD", "ejat-secret", "ejat-maven-password"));
			//
			//			envs.add(createValueEnv("EJAT_RUN_ID", runUUID.toString()));
			//			envs.add(createFieldEnv("EJAT_ENGINE_ID", "metadata.name"));
			//			envs.add(createFieldEnv("EJAT_K8S_NODE", "spec.nodeName"));


			boolean successful = false;
			int retry = 0;
			while(!successful) {
				try {
					//					System.out.println(newPod.toString());
					api.createNamespacedPod(namespace, newPod, "true");

					logger.info("Engine Pod " + newPod.getMetadata().getName() + " started");
					successful = true;
					submittedRuns.inc();
					break;
				} catch(ApiException e) {
					String response = e.getResponseBody();
					if (response.contains("AlreadyExists")) {
						retry++;
						String newEngineName = engineName + "-" + retry;
						newPod.getMetadata().setName(newEngineName);
						logger.info("Engine Pod " + engineName + " already exists, trying with " + newEngineName);
					} else {
						logger.error("Failed to create engine pod :-\n" + e.getResponseBody(),e);
					}
				} catch(Exception e) {
					logger.error("Failed to create engine pod",e);
				}
				logger.info("Waiting 2 seconds before trying to create pod again");
				Thread.sleep(2000);
			}
		} catch(Exception e) {
			logger.error("Failed to start new engine",e);
		}
		return;
	}




	private HashMap<String, Pool> getPools(@NotNull List<IRun> runs) {
		HashMap<String, Pool> pools = new HashMap<>();

		for(IRun run : runs) {
			String poolid = getPoolId(run);
			Pool pool = pools.get(poolid);
			if (pool == null) {
				pool = new Pool(poolid);
			}
			pool.runs.add(run);
		}

		return pools;
	}

	private String getPoolId(IRun run) {
		if (settings.getRequestorsByGroup().contains(run.getRequestor())) {
			return run.getRequestor() + "/" + run.getGroup(); 
		}

		return run.getRequestor();
	}



	public static @NotNull List<V1Pod> getPods(CoreV1Api api, Settings settings) throws K8sControllerException {
		LinkedList<V1Pod> pods = new LinkedList<>();

		try {
			V1PodList list = api.listNamespacedPod(settings.getNamespace(), null, null, null, true, "cirillo-engine-controller=" + settings.getEngineLabel(), null, null, null, null);
			for (V1Pod pod : list.getItems()) {
				pods.add(pod);
			}
		} catch(Exception e) {
			throw new K8sControllerException("Failed retrieving pods",e);
		}

		return pods;
	}

	public static void filterActiveRuns(@NotNull List<V1Pod> pods) {
		Iterator<V1Pod> iPod = pods.iterator();
		while(iPod.hasNext()) {
			V1Pod pod = iPod.next();
			V1PodStatus status = pod.getStatus();

			if (status != null) {
				String phase = status.getPhase();
				if ("failed".equalsIgnoreCase(phase)) {
					iPod.remove();
				} else if ("succeeded".equalsIgnoreCase(phase)) {
					iPod.remove();
				}
			}
		}
	}


	public static void filterTerminated(@NotNull List<V1Pod> pods) {
		Iterator<V1Pod> iPod = pods.iterator();
		while(iPod.hasNext()) {
			V1Pod pod = iPod.next();
			V1PodStatus status = pod.getStatus();

			if (status != null) {
				String phase = status.getPhase();
				if ("failed".equalsIgnoreCase(phase)) {
					continue;
				} else if ("succeeded".equalsIgnoreCase(phase)) {
					continue;
				}
			}
			iPod.remove();
		}
	}


	private static class Pool implements Comparable<Pool> {
		private String id;
		private ArrayList<IRun> runs = new ArrayList<>();

		public Pool(String id) {
			this.id = id;
		}

		@Override
		public int compareTo(Pool o) {
			return runs.size() - o.runs.size();
		}	

	}


	private static class QueuedComparator implements Comparator<IRun> {

		@Override
		public int compare(IRun o1, IRun o2) {
			return o1.getQueued().compareTo(o2.getQueued());
		}

	}


	private V1EnvVar createValueEnv(String name, String value) {
		V1EnvVar env = new V1EnvVar();
		env.setName(name);
		env.setValue(value);

		return env;
	}

	private V1EnvVar createConfigMapEnv(String name, String configMap, String key) {
		V1EnvVar env = new V1EnvVar();
		env.setName(name);

		V1EnvVarSource source = new V1EnvVarSource();
		env.setValueFrom(source);

		V1ConfigMapKeySelector configMapKeyRef = new V1ConfigMapKeySelector();
		source.setConfigMapKeyRef(configMapKeyRef);

		configMapKeyRef.setName(configMap);
		configMapKeyRef.setKey(key);

		return env;
	}

	private V1EnvVar createSecretEnv(String name, String secret, String key) {
		V1EnvVar env = new V1EnvVar();
		env.setName(name);

		V1EnvVarSource source = new V1EnvVarSource();
		env.setValueFrom(source);

		V1SecretKeySelector secretKeyRef = new V1SecretKeySelector();
		source.setSecretKeyRef(secretKeyRef);

		secretKeyRef.setName(secret);
		secretKeyRef.setKey(key);

		return env;
	}

	private V1EnvVar createFieldEnv(String name, String path) {
		V1EnvVar env = new V1EnvVar();
		env.setName(name);

		V1EnvVarSource source = new V1EnvVarSource();
		env.setValueFrom(source);

		V1ObjectFieldSelector fieldKeyRef = new V1ObjectFieldSelector();
		source.setFieldRef(fieldKeyRef);

		fieldKeyRef.setFieldPath(path);

		return env;
	}




}
