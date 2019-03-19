package io.ejat.boot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

public class ITLauncher {

	/**
	 * Test Launcher - runs during integration test verify phase
	 * Requires infrastructure and test OBRs. If unavailable, the test will be allowed to fail
	 */
	@Test
	public void testLauncher() {
		
		System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
		System.out.println("-- ITLauncher#testLauncher()");
		System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
		
		File infrastructureOBRFile = new File("../ejat-obr/target/repository.obr");
		File testOBRFile = new File("../../../ivt/ejat-ivt-parent/ejat-ivt-obr/target/repository.obr");
		String testBundleClass = "io.ejat.ivt/io.ejat.ivt.test.BasicTestExtendedAgain";
		
		String[] args = new String[5];
		args[0] = "--obr";
		args[1] = infrastructureOBRFile.toURI().toString();
		args[2] = "--obr";
		args[3] = testOBRFile.toURI().toString();
		args[4] = testBundleClass;

		// Set logLevel to "DEBUG" or "ALL" for verbose output
		String logLevel = "INFO";
		System.setProperty("log.level", logLevel);

		Launcher launcher = new Launcher();
		try {
			if (infrastructureOBRFile.exists() && testOBRFile.exists()) {
				assertTrue(launcher.launch(args));
			} else {
				assertFalse(launcher.launch(args));
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}