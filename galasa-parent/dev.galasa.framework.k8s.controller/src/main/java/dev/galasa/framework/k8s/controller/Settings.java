package dev.galasa.framework.k8s.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ConfigMap;

public class Settings implements Runnable {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private String namespace;
	private String bootstrap = "http://bootstrap";
	private String podname;
	private String configMapName;
	private String engineLabel = "none";
	private String engineImage = "none";
	private int    engineMemory        = 100;
	private int    engineMemoryRequest = 150;
	private int    engineMemoryLimit   = 200;
	private String nodeArch = "";
	private String nodePreferredAffinity = "";

	private HashSet<String> requiredCapabilities = new HashSet<>();
	private HashSet<String> capableCapabilities = new HashSet<>();
	private String reportCapabilties = null;

	private int    runPoll           = 60;
	private int    runPollRecheck    = 30;
	private int    maxEngines        = 0;
	
	private ArrayList<String> requestorsByScheduleID = new ArrayList<>();
	
	private final CoreV1Api api;
	private String oldConfigMapResourceVersion = "";
	
	public Settings(CoreV1Api api) {
		this.api = api;
		
		loadEnvironmentProperties();
		retrieveConfigMap();
	}
	
	@Override
	public void run() {
		retrieveConfigMap();
	}
	
	
	private void retrieveConfigMap() {

		V1ConfigMap configMap = null;
		try {
			configMap = api.readNamespacedConfigMap(configMapName, namespace, "true", false, false);
		} catch (ApiException e) {
			logger.error("Failed to read configmap '" + configMapName + "' in namespace '" + namespace + "'",e);
			return;
		}

		String newResourceVersion = configMap.getMetadata().getResourceVersion();
		if (newResourceVersion.equals(oldConfigMapResourceVersion)) {
			return;
		}
		oldConfigMapResourceVersion = newResourceVersion;

		logger.info("ConfigMap has been changed, reloading parameters");

		try {
			String newBootstrap = configMap.getData().get("bootstrap");
			if (newBootstrap == null || newBootstrap.trim().isEmpty()) {
				newBootstrap = "http://bootstrap";
			}
			if (!newBootstrap.equals(this.bootstrap)) {
				logger.info("Setting Boostrap from '" + this.bootstrap + "' to '" + newBootstrap + "'");
				this.bootstrap = newBootstrap;
			}
		} catch(Exception e) {
			logger.error("Error processing bootstrap in configmap",e);
		}

		try {
			String maxEnginesString = configMap.getData().get("max_engines");
			if (maxEnginesString == null || maxEnginesString.trim().isEmpty()) {
				maxEnginesString = "1";
			}
			int newMaxEngines = Integer.parseInt(maxEnginesString);
			if (newMaxEngines != maxEngines) {
				logger.info("Setting Max Engines from " + maxEngines + " to " + newMaxEngines);
				maxEngines = newMaxEngines;
			}
		} catch(Exception e) {
			logger.error("Error processing max_engines in configmap",e);
		}

		try {
			String newEngineLabel = configMap.getData().get("engine_label");
			if (newEngineLabel == null || newEngineLabel.trim().isEmpty()) {
				newEngineLabel = "k8s-standard-engine";
			}
			if (!newEngineLabel.equals(engineLabel)) {
				logger.info("Setting Engine Label from '" + engineLabel + "' to '" + newEngineLabel + "'");
				engineLabel = newEngineLabel;
			}
		} catch(Exception e) {
			logger.error("Error processing engine_label in configmap",e);
		}

		try {
			String newEngineImage = configMap.getData().get("engine_image");
			if (newEngineImage == null || newEngineImage.trim().isEmpty()) {
				newEngineImage = "cicsts-docker-local.artifactory.swg-devops.com/galasav3-boot-embedded";
			}
			if (!newEngineImage.equals(engineImage)) {
				logger.info("Setting Engine Image from '" + engineImage + "' to '" + newEngineImage + "'");
				engineImage = newEngineImage;
			}
		} catch(Exception e) {
			logger.error("Error processing engine_image in configmap",e);
		}

		try {
			String newEngineMemory = configMap.getData().get("engine_memory");
			if (newEngineMemory == null || newEngineMemory.trim().isEmpty()) {
				newEngineMemory = "300";
			}
			Integer memory = Integer.parseInt(newEngineMemory);
			if (memory != engineMemory) {
				logger.info("Setting Engine Memory from '" + engineMemory + "' to '" + memory + "'");
				engineMemory = memory;
			}
		} catch(Exception e) {
			logger.error("Error processing engine_memory in configmap",e);
		}

		try {
			String newEngineMemoryRequest = configMap.getData().get("engine_memory_request");
			if (newEngineMemoryRequest == null || newEngineMemoryRequest.trim().isEmpty()) {
				newEngineMemoryRequest = Integer.toString(engineMemory + 50);
			}
			Integer memory = Integer.parseInt(newEngineMemoryRequest);
			if (memory != engineMemoryRequest) {
				logger.info("Setting Engine Memory Request from '" + engineMemoryRequest + "' to '" + memory + "'");
				engineMemoryRequest = memory;
			}
		} catch(Exception e) {
			logger.error("Error processing engine_memory_request in configmap",e);
		}

		try {
			String newEngineMemoryLimit = configMap.getData().get("engine_memory_limit");
			if (newEngineMemoryLimit == null || newEngineMemoryLimit.trim().isEmpty()) {
				newEngineMemoryLimit = Integer.toString(engineMemory + 100);
			}
			Integer memory = Integer.parseInt(newEngineMemoryLimit);
			if (memory != engineMemoryLimit) {
				logger.info("Setting Engine Memory Limit from '" + engineMemoryLimit + "' to '" + memory + "'");
				engineMemoryLimit = memory;
			}
		} catch(Exception e) {
			logger.error("Error processing engine_memory_limit in configmap",e);
		}

		try {
			String newRunPoll = configMap.getData().get("run_poll");
			if (newRunPoll == null || newRunPoll.trim().isEmpty()) {
				newRunPoll = "20";
			}
			Integer poll = Integer.parseInt(newRunPoll);
			if (poll != runPoll) {
				logger.info("Setting Run Poll from '" + runPoll + "' to '" + poll + "'");
				runPoll = poll;
			}
		} catch(Exception e) {
			logger.error("Error processing run_poll in configmap",e);
		}

		try {
			String newRunPoll = configMap.getData().get("run_poll_recheck");
			if (newRunPoll == null || newRunPoll.trim().isEmpty()) {
				newRunPoll = "5";
			}
			Integer poll = Integer.parseInt(newRunPoll);
			if (poll != runPollRecheck) {
				logger.info("Setting Run Poll Recheck from '" + runPollRecheck + "' to '" + poll + "'");
				runPollRecheck = poll;
			}
		} catch(Exception e) {
			logger.error("Error processing run_poll_recheck in configmap",e);
		}

		try {
			String newRequestors = configMap.getData().get("scheduled_requestors");
			if (newRequestors == null || newRequestors.trim().isEmpty()) {
				newRequestors = "";
			}
			ArrayList<String> newRequestorsByScheduleid = new ArrayList<>();

			String requestors[] = newRequestors.split(",");	
			for(String requestor : requestors) {
				newRequestorsByScheduleid.add(requestor);
			}

			if (!requestorsByScheduleID.equals(newRequestorsByScheduleid)) {
				logger.info("Setting Requestors by Schedule from '" + requestorsByScheduleID + "' to '" + newRequestorsByScheduleid + "'");
				requestorsByScheduleID = newRequestorsByScheduleid;
			}
		} catch(Exception e) {
			logger.error("Error processing scheduled_requestors in configmap",e);
		}

		try {
			String newCapabilities = configMap.getData().get("engine_capabilities");
			if (newCapabilities == null || newCapabilities.trim().isEmpty()) {
				newCapabilities = "";
			}
			ArrayList<String> newRequiredCapabilties = new ArrayList<>();
			ArrayList<String> newCapableCapabilties = new ArrayList<>();

			String capabalities[] = newCapabilities.split(",");	
			for(String capability : capabalities) {
				capability = capability.trim();
				if (capability.startsWith("+")) {
					capability = capability.substring(1);
					if (!capability.isEmpty()) {
						newRequiredCapabilties.add(capability);
					}
				} else {
					if (!capability.isEmpty()) {
						newCapableCapabilties.add(capability);
					}
				}
			}

			boolean changed = false;
			if (newRequiredCapabilties.size() != requiredCapabilities.size()
					|| newCapableCapabilties.size() != capableCapabilities.size()) {
				changed = true;
			} else {
				for (String cap : newCapableCapabilties) {
					if (!capableCapabilities.contains(cap)) {
						changed = true;
						break;
					}
				}
				for (String cap : newRequiredCapabilties) {
					if (!requiredCapabilities.contains(cap)) {
						changed = true;
						break;
					}
				}
			}

			if (changed) {
				capableCapabilities.clear();
				requiredCapabilities.clear();
				capableCapabilities.addAll(newCapableCapabilties);
				requiredCapabilities.addAll(newRequiredCapabilties);
				logger.info("Engine set with Required Capabilities - " + requiredCapabilities);
				logger.info("Engine set with Capabable Capabilities - " + capableCapabilities);

				StringBuilder report = new StringBuilder();
				for(String cap : requiredCapabilities) {
					if (report.length() > 0) {
						report.append(",");
					}
					report.append("+");
					report.append(cap);
				}
				for(String cap : capableCapabilities) {
					if (report.length() > 0) {
						report.append(",");
					}
					report.append(cap);
				}
				if (report.length() > 0) {
					reportCapabilties = report.toString();
				} else {
					reportCapabilties = null;
				}

			}
		} catch(Exception e) {
			logger.error("Error processing engine_capabilities in configmap",e);
		}


		try {
			String newNodeArch = configMap.getData().get("node_arch");
			if (newNodeArch == null) {
				newNodeArch = "";
			}
			newNodeArch = newNodeArch.trim();
			if (!newNodeArch.equals(nodeArch)) {
				logger.info("Setting Node Architecture from '" + nodeArch + "' to '" + newNodeArch + "'");
				nodeArch = newNodeArch;
			}
		} catch(Exception e) {
			logger.error("Error processing node_arch in configmap",e);
		}

		try {
			String newNodePreferredAffinity = configMap.getData().get("galasa_node_preferred_affinity");
			if (newNodePreferredAffinity == null) {
				newNodePreferredAffinity = "";
			}
			newNodePreferredAffinity = newNodePreferredAffinity.trim();
			if (!newNodePreferredAffinity.equals(nodePreferredAffinity)) {
				logger.info("Setting Node Preferred Affinity from '" + nodePreferredAffinity + "' to '" + newNodePreferredAffinity + "'");
				nodePreferredAffinity = newNodePreferredAffinity;
			}
		} catch(Exception e) {
			logger.error("Error processing node_preferred_affinity in configmap",e);
		}

		return;
	}

	
	
	private void loadEnvironmentProperties() {

		namespace = System.getenv("NAMESPACE");
		if (namespace == null || namespace.trim().isEmpty()) {
			namespace = "default";
		} else {
			namespace = namespace.trim();
		}
		logger.info("Setting Namespace to '" + namespace + "'");

		podname = System.getenv("PODNAME");
		if (podname == null || podname.trim().isEmpty()) {
			podname = "k8s-controller";
		} else {
			podname = podname.trim();
		}
		logger.info("Setting Pod Name to '" + podname + "'");

		configMapName = System.getenv("CONFIG");
		if (configMapName == null || configMapName.trim().isEmpty()) {
			configMapName = "config";
		} else {
			configMapName = configMapName.trim();
		}
		logger.info("Setting ConfigMap to '" + configMapName + "'");

		return;
	}

	public String getPodName() {
		return this.podname;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public String getEngineLabel() {
		return this.engineLabel;
	}

	public int getMaxEngines() {
		return this.maxEngines;
	}


	public List<String> getRequestorsByGroup() {
		return this.requestorsByScheduleID;
	}

	public String getNodeArch() {
		return this.nodeArch;
	}

	public String getNodePreferredAffinity() {
		return this.nodePreferredAffinity;
	}

	public String getEngineImage() {
		return this.engineImage;
	}

	public int getEngineMemoryRequest() {
		return this.engineMemoryRequest;
	}

	public int getEngineMemoryLimit() {
		return this.engineMemoryLimit;
	}

	public int getEngineMemory() {
		return this.engineMemory;
	}

	public String getBootstrap() {
		return this.bootstrap;
	}

}
