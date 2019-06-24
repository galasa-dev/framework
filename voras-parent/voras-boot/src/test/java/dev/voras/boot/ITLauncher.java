package dev.voras.boot;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class ITLauncher {
	
	/**
	 * Test Launcher - runs during integration test verify phase
	 * Requires infrastructure and test OBRs. If unavailable, the test will be allowed to fail
	 * @throws InterruptedException 
	 * @thr9ows LauncherException 
	 */
	@Test
	public void testLauncher() {
		
		Path localmavenPath = Paths.get(System.getProperty("user.home"), ".m2", "repository");
		if (System.getenv("JENKINS_HOME") != null) {
			localmavenPath = Paths.get(System.getenv("WORKSPACE"), ".repository");
		}
		
		String obrOption = "--obr";
		
		String[] args = new String[11];
		args[0] = "--trace";
		args[1] = "--localmaven";
		args[2] = Paths.get(localmavenPath.toFile().getAbsolutePath()).toUri().toString();
		args[3] = obrOption;
		args[4] = "mvn:dev.voras/dev.voras.framework.obr/0.3.0-SNAPSHOT/obr";
		args[5] = obrOption;
		args[6] = "mvn:dev.voras/dev.voras.core.obr/0.3.0-SNAPSHOT/obr";
		args[7] = obrOption;
		args[8] = "mvn:dev.voras/dev.voras.ivt.obr/0.3.0-SNAPSHOT/obr";
		args[9] = "--test";
		args[10] = "dev.voras.ivt.core/dev.voras.ivt.test.BasicTestExtendedAgain";

		Launcher launcher = new Launcher();
		try {
			launcher.launch(args);
		} catch (Exception e) {
			// Ignore failure until we have the Maven repositories set up
			if (!e.getCause().getCause().toString().contains("Unable to locate maven artifact mvn:dev.voras/dev.voras.core.obr")) {
				fail(e.getMessage());
			}
		}
	}
}