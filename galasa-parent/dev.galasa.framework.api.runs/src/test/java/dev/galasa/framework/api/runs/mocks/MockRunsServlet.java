/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.mocks;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.common.mocks.IServletUnderTest;
import dev.galasa.framework.api.runs.RunsServlet;
import dev.galasa.framework.spi.IFramework;

public class MockRunsServlet extends RunsServlet implements IServletUnderTest {
    
	@Override
	public void setFramework(IFramework framework) {
		super.setFramework(framework);
	}

	@Override
	public void setFileSystem(IFileSystem fileSystem) {
		throw new UnsupportedOperationException("Unimplemented method 'setFileSystem'");
	}

	public IFramework getFramework() {
		return super.getFramework();
	}
}