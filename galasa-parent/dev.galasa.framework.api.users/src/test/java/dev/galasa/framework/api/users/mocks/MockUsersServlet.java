/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.mocks;

import dev.galasa.framework.api.users.UsersServlet;
import dev.galasa.framework.spi.IFramework;

public class MockUsersServlet extends UsersServlet{

	@Override
	public void setFramework(IFramework framework) {
		super.setFramework(framework);
	}

	public IFramework getFramework() {
		return super.getFramework();
	}
}
