package io.ejat.framework;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.AbstractManager;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.FrameworkException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IFrameworkRuns;
import io.ejat.framework.spi.IRun;

public class FrameworkRuns implements IFrameworkRuns {

	private final Pattern  runPattern = Pattern.compile("^\\Qrun.\\E(\\w+)\\Q.\\E.*$");

	private final IFramework                         framework;
	private final IDynamicStatusStoreService         dss;
	private final IConfigurationPropertyStoreService cps;

	public FrameworkRuns(Framework framework) throws FrameworkException {
		this.framework = framework;
		this.dss       = framework.getDynamicStatusStoreService("framework");
		this.cps       = framework.getConfigurationPropertyService("framework");
	}

	@Override
	public List<IRun> getActiveRuns() throws FrameworkException {

		List<IRun> runs = getAllRuns();
		Iterator<IRun> iruns = runs.iterator();
		while(iruns.hasNext()) {
			IRun run = iruns.next();

			if (run.getHeartbeat() == null) {
				iruns.remove();
			}
		}

		return runs;
	}

	@Override
	public List<IRun> getAllRuns() throws FrameworkException {
		HashMap<String, IRun> runs = new HashMap<>();

		Map<String, String> runProperties = dss.getPrefix("run.");
		for(String key : runProperties.keySet()) {
			Matcher matcher = runPattern.matcher(key);
			if (matcher.find()) {
				String runName = matcher.group(1);

				if (!runs.containsKey(runName)) {
					runs.put(runName, new RunImpl(runName, this.dss));
				}
			}
		}

		LinkedList<IRun> returnRuns = new LinkedList<>(runs.values());

		return returnRuns;
	}

	@Override
	public @NotNull Set<String> getActiveRunNames() throws FrameworkException {
		List<IRun> runs = getActiveRuns();

		HashSet<String> runNames = new HashSet<>();
		for(IRun run : runs) {
			runNames.add(run.getName());
		}

		return runNames;
	}

	@Override
	@NotNull
	public @NotNull IRun submitRun(String runType, 
			String requestor, 
			@NotNull String bundleName, 
			@NotNull String testName, 
			String mavenRepository, 
			String obr,
			String stream,
			boolean local)
					throws FrameworkException {
		if (testName == null) {
			throw new FrameworkException("Missing test name");
		}
		if (bundleName == null) {
			throw new FrameworkException("Missing bundle name");
		}

		String bundleTest = bundleName + "/" + testName;

		runType = AbstractManager.nulled(runType);
		if (runType == null) {
			runType = "unknown";
		}
		requestor = AbstractManager.nulled(requestor);
		if (requestor == null) {
			requestor = "unknown";
		}
		stream = AbstractManager.nulled(stream);
		if (stream == null) {
			stream = "live";
		}


		String runName = null;

		//*** Allocate the next number for the run type

		//*** Get the prefix of this run type
		String typePrefix = AbstractManager.nulled(this.cps.getProperty("request.type." + runType,  "prefix"));
		if (typePrefix == null) {
			if ("local".equals(runType)) {
				typePrefix = "L";
			} else {
				typePrefix = "U"; //*** For unknown prefix
			}
		}

		//*** Get the maximum number for this prefix
		int maxNumber = Integer.MAX_VALUE;
		String sMaxNumber = AbstractManager.nulled(this.cps.getProperty("request.prefix", "maximum", typePrefix));
		if (sMaxNumber != null) {
			maxNumber = Integer.parseInt(sMaxNumber);
		}

		try {
			//*** Now loop until we find the next free number for this run type
			boolean maxlooped = false;
			while(runName == null) {
				String pLastused = "request.prefix." + typePrefix + ".lastused";
				String sLatestNumber = this.dss.get(pLastused);
				int latestNumber = 0;
				if (sLatestNumber != null && !sLatestNumber.trim().isEmpty()) {
					latestNumber = Integer.parseInt(sLatestNumber);
				}

				//*** Add 1 to the run number and see if we get it
				latestNumber++;
				if (latestNumber > maxNumber) { //*** have we gone past the maximum number
					if (maxlooped) {
						throw new FrameworkException("Not enough request type numbers available, looped twice");
					}
					latestNumber = 1;
					maxlooped = true; //*** Safety check to make sure we havent gone through all the numbers again
				}

				String sNewNumber = Integer.toString(latestNumber);
				if (!this.dss.putSwap(pLastused, sLatestNumber, sNewNumber)) {
					Thread.sleep(this.framework.getRandom().nextInt(200)); //*** Wait for a bit, to avoid race conditions
					continue;    //  Try again with the new latest number
				}

				String tempRunName = typePrefix + sNewNumber;

				//*** Set up the otherRunProperties that will go with the Run number
				HashMap<String, String> otherRunProperties = new HashMap<>();
				otherRunProperties.put("run." + tempRunName + ".status", "queued");
				otherRunProperties.put("run." + tempRunName + ".testbundle", bundleName);
				otherRunProperties.put("run." + tempRunName + ".testclass", testName);
				otherRunProperties.put("run." + tempRunName + ".request.type", runType);
				otherRunProperties.put("run." + tempRunName + ".local", Boolean.toString(local));
				if (mavenRepository != null) {
					otherRunProperties.put("run." + tempRunName + ".repository", mavenRepository);
				}
				if (obr != null) {
					otherRunProperties.put("run." + tempRunName + ".obr", obr);
				}
				otherRunProperties.put("run." + tempRunName + ".stream", stream);
				otherRunProperties.put("run." + tempRunName + ".requestor", requestor.toLowerCase());

				//*** See if we can setup the runnumber properties (clashes possible if low max number or sharing prefix
				if (!this.dss.putSwap("run." + tempRunName + ".test", null, bundleTest, otherRunProperties)) {
					Thread.sleep(this.framework.getRandom().nextInt(200)); //*** Wait for a bit, to avoid race conditions
					continue; //*** Try again
				}

				runName = tempRunName; //*** Got it					
			}
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new FrameworkException("Interrupted", e);
		} catch (Exception e) {
			throw new FrameworkException("Problem submitting job", e);
		}

		return new RunImpl(runName, this.dss);
	}

	@Override
	public boolean delete(String runname) throws DynamicStatusStoreException {
		String prefix = "run." + runname + ".";
				
		Map<String, String> properties = this.dss.getPrefix(prefix);
		if (properties.isEmpty()) {
			return false;
		}
		
		this.dss.deletePrefix(prefix);
		return true;
	}

	@Override
	public boolean reset(String runname) throws DynamicStatusStoreException {
		String prefix = "run." + runname + ".";
				
		Map<String, String> properties = this.dss.getPrefix(prefix);
		if (properties.isEmpty()) {
			return false;
		}
		
		if ("true".equals(properties.get(prefix + "local"))) {
			return false;
		}
		
		this.dss.delete(prefix + "heartbeat");
		this.dss.put(prefix + "status", "queued");
		return true;
	}

	@Override
	public IRun getRun(String runname) throws DynamicStatusStoreException {
		String prefix = "run." + runname + ".";
		
		Map<String, String> properties = this.dss.getPrefix(prefix);
		if (properties.isEmpty()) {
			return null;
		}
		
		return new RunImpl(runname, this.dss);
	}



}
