/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.ras.internal.mocks.MockRunResult;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockPath;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class RasServletTest extends BaseServletTest {

	protected MockFileSystem mockFileSystem;

	protected List<IRunResult> generateTestData(String runId, String runName, String runLog) {
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();

		// Build the results the DB will return.
		String requestor = "galasa";

		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setResult("Passed");

		Path artifactRoot = new MockPath("/" + runName, this.mockFileSystem);
		IRunResult result = new MockRunResult( runId, testStructure, artifactRoot, runLog);
		mockInputRunResults.add(result);

		return mockInputRunResults;
	}
}