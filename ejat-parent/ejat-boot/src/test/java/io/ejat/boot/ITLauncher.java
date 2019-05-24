package io.ejat.boot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ITLauncher {
	
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * Test Launcher - runs during integration test verify phase
	 * Requires infrastructure and test OBRs. If unavailable, the test will be allowed to fail
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws LauncherException 
	 */
	@Test
	public void testLauncher() throws IOException, LauncherException, InterruptedException {
		
		File infrastructureOBRFile = new File("../ejat-obr/target/repository.obr");
		File testOBRFile = new File("../../../ivt/ejat-ivt-parent/ejat-ivt-obr/target/repository.obr");
		String testBundleClass = "io.ejat.ivt/io.ejat.ivt.test.BasicTestExtendedAgain";
		
		File bootstrapPropertiesFile = tempFolder.newFile("bootstrap.properties");
		File cpsPropertiesFile = tempFolder.newFile("cps.properties");
		File dssPropertiesFile = tempFolder.newFile("dss.properties");
		File rasDiretory = tempFolder.newFolder("ras");
		
		String bootstrapPropertiesFileUrl = bootstrapPropertiesFile.toURI().toURL().toString();
		String cpsPropertiesFileUrl = cpsPropertiesFile.toURI().toURL().toString();
		String dssPropertiesFileUrl = dssPropertiesFile.toURI().toURL().toString();
		String rasDirectoryUrl = rasDiretory.toURI().toURL().toString();
		
		OutputStream bootstrapPropertiesFileOutputStream = new FileOutputStream(bootstrapPropertiesFile);
		
		Properties bootstrapProperties = new Properties();
		bootstrapProperties.setProperty("framework.config.store", cpsPropertiesFileUrl);
		bootstrapProperties.store(bootstrapPropertiesFileOutputStream, null);
		bootstrapPropertiesFileOutputStream.close();
		
		FileOutputStream cpsPropertiesFileOutputStream = new FileOutputStream(cpsPropertiesFile);
		Properties cpsProperties = new Properties();
		cpsProperties.put("framework.dynamicstatus.store", dssPropertiesFileUrl);
		cpsProperties.put("framework.resultarchive.store", rasDirectoryUrl);
		cpsProperties.put("framework.run.name", "DUMMY");
		cpsProperties.store(cpsPropertiesFileOutputStream, null);
		cpsPropertiesFileOutputStream.close();
		
		String[] args = new String[7];
		args[0] = "--obr";
		args[1] = infrastructureOBRFile.toURI().toString();
		args[2] = "--obr";
		args[3] = testOBRFile.toURI().toString();
		args[4] = "--bootstrap";
		args[5] = bootstrapPropertiesFileUrl;
		args[6] = testBundleClass;

		// Set logLevel to "DEBUG" or "ALL" for verbose output
		String logLevel = "INFO";
		System.setProperty("log.level", logLevel);

		Launcher launcher = new Launcher();
		if (infrastructureOBRFile.exists() && testOBRFile.exists()) {
			assertTrue(launcher.launch(args));
		} else {
			assertFalse(launcher.launch(args));
		}
	}
}