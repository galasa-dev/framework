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

import io.ejat.framework.spi.FrameworkException;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFrameworkRuns;
import io.ejat.framework.spi.IRun;

public class FrameworkRuns implements IFrameworkRuns {
	
	private final Pattern  runPattern = Pattern.compile("^\\Qrun.\\E(\\w+)\\Q.\\E.*$");

	private IDynamicStatusStoreService dss;

	public FrameworkRuns(Framework framework) throws FrameworkException {
		this.dss = framework.getDynamicStatusStoreService("framework");
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
					runs.put(runName, new RunImpl(runName, runProperties));
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
	
	

}
