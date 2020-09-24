/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.internal.ras.directory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class DirectoryRASDirectoryService implements IResultArchiveStoreDirectoryService {

	private final Path baseDirectory;
	private final Gson gson;

	protected DirectoryRASDirectoryService(@NotNull Path baseDirectory, Gson gson) {
		this.baseDirectory = baseDirectory;
		this.gson = gson;
	}
	
	@Override
	public @NotNull List<IRunResult> getRuns(@NotNull IRasSearchCriteria... searchCriteria) throws ResultArchiveStoreException{
		
		ArrayList<IRunResult> runs = new ArrayList<>();
		
		List<DirectoryRASRunResult> allRuns = getAllRuns();
		
		boolean matched = true;
	
		for(DirectoryRASRunResult run : allRuns) {
			for(IRasSearchCriteria criteria : searchCriteria) {
				if(!criteria.criteriaMatched(run.getTestStructure())) {
					matched = false;
					break;
				}
			}
			if(matched) {
				runs.add(run);
			}
		}
		
		return runs;
	}

	@Override
	public @NotNull List<IRunResult> getRuns(@NotNull String runName) throws ResultArchiveStoreException {

		ArrayList<IRunResult> runs = new ArrayList<>();

		List<DirectoryRASRunResult> allRuns = getAllRuns();

		for (DirectoryRASRunResult run : allRuns) {
			if (run.getTestStructure().getRunName().equals(runName)) {
				runs.add(run);
			}
		}

		return runs;
	}

	@Override
	public @NotNull String getName() {
		return "Local " + this.baseDirectory.toString();
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public @NotNull List<IRunResult> getRuns(String requestor, Instant from, Instant to, String testName)
			throws ResultArchiveStoreException {

		ArrayList<IRunResult> runs = new ArrayList<>();

		List<DirectoryRASRunResult> allRuns = getAllRuns();

		for (DirectoryRASRunResult result : allRuns) {
			TestStructure testStructure = result.getTestStructure();

			if (requestor != null) {
				if (!requestor.equals(testStructure.getRequestor())) {
					continue;
				}
			}
			
			if(testName != null) {
				if(!testName.equals(testStructure.getTestName())) {
					continue;
				}
			}
			

			Instant queued = testStructure.getQueued();

			if (from != null) {
				if (from.compareTo(queued) > 0) {
					continue;
				}
			}

			if (to != null) {
				if (to.compareTo(queued) <= 0) {
					continue;
				}
			}

			runs.add(result);
		}

		return runs;
	}

	@Override
	public @NotNull List<String> getRequestors() throws ResultArchiveStoreException {
		HashSet<String> requestors = new HashSet<>();

		for (DirectoryRASRunResult result : getAllRuns()) {
			if(result!=null) {
				TestStructure testStructure = result.getTestStructure();
				if(testStructure != null && testStructure.getTestName()!=null) {
					requestors.add(testStructure.getRequestor());
				}

			}
		}

		return new ArrayList<>(requestors);
	}

	@Override
	public @NotNull List<RasTestClass> getTests() throws ResultArchiveStoreException {
		HashMap<String,RasTestClass> tests = new HashMap<>();
		String key;
		for (DirectoryRASRunResult result : getAllRuns()) {
			if(result != null) {

				TestStructure testStructure = result.getTestStructure();
				if(testStructure != null && testStructure.getTestName()!=null) {
					key = testStructure.getBundle()+"/"+testStructure.getTestName();
					if(!tests.containsKey(key)){
						tests.put(key,new RasTestClass(testStructure.getTestName(), testStructure.getBundle()));
					}
				}
			}
		}

		return new ArrayList<>(tests.values());
	}

	 @NotNull
	 protected List<DirectoryRASRunResult> getAllRuns() throws ResultArchiveStoreException {
		try {
			ArrayList<DirectoryRASRunResult> runs = new ArrayList<>();

			try (Stream<Path> stream = Files.list(baseDirectory)) {
				stream.forEach(new ConsumeRuns(runs, gson));
			}

			return runs;
		} catch (Throwable t) {
			throw new ResultArchiveStoreException("Unable to obtain runs", t);
		}
	}

	private static class ConsumeRuns implements Consumer<Path> {

		private final List<DirectoryRASRunResult> results;
		private final Gson                        gson;

		private static final Log                  logger = LogFactory.getLog(ConsumeRuns.class);

		public ConsumeRuns(List<DirectoryRASRunResult> results, Gson gson) {
			this.results = results;
			this.gson = gson;
		}

		@Override
		public void accept(Path path) {
			if (!Files.isDirectory(path)) {
				return;
			}

			Path structureFile = path.resolve("structure.json");
			if (Files.exists(structureFile)) {
				try {
					results.add(new DirectoryRASRunResult(path, gson));
				} catch (Throwable t) {
					logger.trace("Unable to create a run result from " + structureFile.toString());
				}
			}

		}

	}

}
