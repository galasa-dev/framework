package dev.voras.framework.internal.creds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Random;

import javax.validation.constraints.NotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.voras.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.voras.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.voras.framework.internal.creds.FileCredentialsStore;
import dev.voras.framework.internal.creds.FrameworkCredentialsService;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.DynamicStatusStoreException;
import dev.voras.framework.spi.FrameworkException;
import dev.voras.framework.spi.IConfidentialTextService;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IFrameworkRuns;
import dev.voras.framework.spi.IResourcePoolingService;
import dev.voras.framework.spi.IResultArchiveStore;
import dev.voras.framework.spi.IRun;
import dev.voras.framework.spi.creds.CredentialsException;
import dev.voras.framework.spi.creds.CredentialsToken;
import dev.voras.framework.spi.creds.ICredentials;
import dev.voras.framework.spi.creds.ICredentialsService;

/**
 * <p>This test class checks the behaviour of retrieving credentials from the local store is functional.</p>
 * 
 * @author Bruce Abbott
 */
public class FrameworkCredentialsTest {

	private File fileCPS;
	private File fileCREDS;

	@Before
	public void setup() throws Exception {
		fileCPS = File.createTempFile("ejatcps_", ".properties");
		fileCREDS = File.createTempFile("ejatcreds_", ".properties");
	}

	@After
	public void teardown() throws Exception {
		if (fileCPS.exists()) {
			fileCPS.delete();
		}
		if (fileCREDS.exists()) {
			fileCREDS.delete();
		}
	}

	/**
	 * This test method checks that a token stored in the local Credentials Store can be retrieved.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetCredentialsToken() throws Exception {

		Properties propsCPS = new Properties();
		saveProperties(propsCPS, fileCPS);

		Properties propsCREDS = new Properties();
		propsCREDS.setProperty("secure.credentials.testcredsid.token", "testToken");
		saveProperties(propsCREDS, fileCREDS);


		Framework framework = new Framework(fileCPS);
		FileCredentialsStore fileCreds = new FileCredentialsStore(fileCREDS.toURI(), framework);
		FrameworkCredentialsService service = new FrameworkCredentialsService(framework, fileCreds);


		ICredentials creds = service.getCredentials("testcredsid");

		assertNotNull("Token credentials was not found", creds);

		CredentialsToken credsToken = (CredentialsToken) creds;

		assertEquals("Token creds incorrect value", "testToken", credsToken.getToken());
	}

	private void saveProperties(Properties properties, File file) throws Exception {
		FileOutputStream out = new FileOutputStream(file);
		properties.store(out, null);
		out.close();
	}


	/**
	 * <p>This is a private class used to implement the IFramework for testing purposes.</p>
	 */
	private class Framework implements IFramework{
		private Properties overrides = new Properties();
		private Properties records = new Properties();
		private File cps;

		public Framework(File fileCPS) {
			this.cps = fileCPS;
		}

		public IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace) throws ConfigurationPropertyStoreException {
			FrameworkConfigurationPropertyService fcps;
			try {
				fcps = new FrameworkConfigurationPropertyService(this, new FpfConfigurationPropertyStore(cps.toURI()) , overrides, records, namespace);
				return fcps;
			} catch (Exception e) {
				throw new ConfigurationPropertyStoreException("error initialising", e);
			}
		}

		public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace) throws DynamicStatusStoreException {
			return null;
		}
		public IResultArchiveStore getResultArchiveStore(){return null;}
		public IResourcePoolingService getResourcePoolingService(){return null;}

		@Override
		public @NotNull IConfidentialTextService getConfidentialTextService() {return null;}

		@Override
		public String getTestRunName() {
			return null;
		}

		@Override
		public ICredentialsService getCredentialsService() throws CredentialsException {
			return null;
		}

		@Override
		public Random getRandom() {
			return null;
		}

		@Override
		public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
			return null;
		}

		@Override
		public void setFrameworkProperties(Properties overrideProperties) {
		}

		@Override
		public boolean isInitialised() {
			return false;
		}

		@Override
		public void initialisationComplete() {
		}

		@Override
		public IRun getTestRun() {
			return null;
		}

		@Override
		public Properties getRecordProperties() {
			return null;
		}

	} 

}