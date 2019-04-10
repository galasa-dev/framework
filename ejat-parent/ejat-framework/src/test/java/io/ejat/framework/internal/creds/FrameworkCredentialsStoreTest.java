package io.ejat.framework.internal.creds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.FileNotFoundException;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import io.ejat.framework.spi.IConfidentialTextService;
import io.ejat.framework.internal.cps.FpfConfigurationPropertyStore;
import io.ejat.framework.internal.cps.FrameworkConfigurationPropertyService;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IResourcePoolingService;
import io.ejat.framework.spi.IResultArchiveStore;
import io.ejat.framework.spi.creds.ICredentialsStoreService;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import java.security.NoSuchAlgorithmException;
import io.ejat.framework.spi.creds.ICredentials;
import io.ejat.framework.spi.creds.FileCredentialsToken;
import io.ejat.framework.spi.creds.FileCredentialsUsernamePassword;
import io.ejat.framework.spi.creds.FileCredentialsUsername;

import javax.crypto.IllegalBlockSizeException;

/**
 * <p>This test class checks the behaviour of retrieving credentials from the framework is functional.</p>
 * 
 * @author Bruce Abbott
 */
public class FrameworkCredentialsStoreTest {

    /**
     * <p>This test method checks that a Credentials Store can be constructed in the framework.</p>
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void testFrameworkConstructor() throws ConfigurationPropertyStoreException, IOException, NoSuchAlgorithmException {
        Framework framework = new Framework();
        Properties overrides = new Properties();

        File testFile1 = File.createTempFile("ejatfpf_1", ".properties");

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testFile1.toURI());

        File testFile2 = File.createTempFile("ejatfpf_2", ".properties");
 
        FileCredentialsStore fileCreds = new FileCredentialsStore(testFile2.toURI(), framework);

        FrameworkCredentialsStoreService test = new FrameworkCredentialsStoreService(framework, fpfcps, fileCreds, overrides);
        assertNotNull("Framework credentials store could not be created", test);
    }

    /**
     * <p>This test method checks that a token stored in the Credentials Store can be retrieved by the framework.</p>
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws CredentialsStoreException
     */
    @Test
    public void testFrameworkGetPropertyToken() throws ConfigurationPropertyStoreException, IOException, NoSuchAlgorithmException, FileNotFoundException, CredentialsStoreException, IllegalBlockSizeException {
        Framework framework = new Framework();
        Properties overrides = new Properties();

        File testFile1 = File.createTempFile("ejatfpf_1", ".properties");
        Properties testProps1 = new Properties();
        testProps1.setProperty("framework.credentials.file.encryption.key", "");
        FileOutputStream out1 = new FileOutputStream(testFile1);
        testProps1.store(out1, null);
        out1.close();
        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testFile1.toURI());

        File testFile2 = File.createTempFile("ejatfpf_2", ".properties");
        Properties testProps2 = new Properties();
        testProps2.setProperty("framework.secure.credentials.credsID.token", "testToken");
        FileOutputStream out2 = new FileOutputStream(testFile2);
        testProps2.store(out2, null);
        out2.close();
        FileCredentialsStore fileCreds = new FileCredentialsStore(testFile2.toURI(), framework);

        FrameworkCredentialsStoreService test = new FrameworkCredentialsStoreService(framework, fpfcps, fileCreds, overrides);
        ICredentials creds = test.getCredentials("credsID");

        FileCredentialsToken credsToken = new FileCredentialsToken("");
        if (creds instanceof FileCredentialsToken) {
            credsToken = (FileCredentialsToken) creds;
        }

        assertEquals("Did not return expected class", "class io.ejat.framework.spi.creds.FileCredentialsToken", creds.getClass().toString());
        assertEquals("Did not return expected value", "testToken", credsToken.getToken());
    }

    /**
     * <p>This test method checks that a username/password stored in the Credentials Store can be retrieved by the framework.</p>
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws CredentialsStoreException
     */
    @Test
    public void testFrameworkGetPropertyUsernamePassword() throws ConfigurationPropertyStoreException, IOException, NoSuchAlgorithmException, FileNotFoundException, CredentialsStoreException, IllegalBlockSizeException {
        Framework framework = new Framework();
        Properties overrides = new Properties();

        File testFile1 = File.createTempFile("ejatfpf_1", ".properties");
        Properties testProps1 = new Properties();
        testProps1.setProperty("framework.credentials.file.encryption.key", "");
        FileOutputStream out1 = new FileOutputStream(testFile1);
        testProps1.store(out1, null);
        out1.close();
        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testFile1.toURI());

        File testFile2 = File.createTempFile("ejatfpf_2", ".properties");
        Properties testProps2 = new Properties();
        testProps2.setProperty("framework.secure.credentials.credsID.username", "testUsername");
        testProps2.setProperty("framework.secure.credentials.credsID.password", "testPassword");
        FileOutputStream out2 = new FileOutputStream(testFile2);
        testProps2.store(out2, null);
        out2.close();
        FileCredentialsStore fileCreds = new FileCredentialsStore(testFile2.toURI(), framework);

        FrameworkCredentialsStoreService test = new FrameworkCredentialsStoreService(framework, fpfcps, fileCreds, overrides);
        ICredentials creds = test.getCredentials("credsID");

        FileCredentialsUsernamePassword credsUsernamePassword = new FileCredentialsUsernamePassword("", "");
        if (creds instanceof FileCredentialsUsernamePassword) {
            credsUsernamePassword = (FileCredentialsUsernamePassword) creds;
        }

        assertEquals("Did not return expected class", "class io.ejat.framework.spi.creds.FileCredentialsUsernamePassword", creds.getClass().toString());
        assertEquals("Did not return expected value", "testUsername", credsUsernamePassword.getUsername());
        assertEquals("Did not return expected value", "testPassword", credsUsernamePassword.getPassword());
    }

    /**
     * <p>This test method checks that a username stored in the Credentials Store can be retrieved by the framework.</p>
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws CredentialsStoreException
     */
    @Test
    public void testFrameworkGetPropertyUsername() throws ConfigurationPropertyStoreException, IOException, NoSuchAlgorithmException, FileNotFoundException, CredentialsStoreException, IllegalBlockSizeException {
        Framework framework = new Framework();
        Properties overrides = new Properties();

        File testFile1 = File.createTempFile("ejatfpf_1", ".properties");
        Properties testProps1 = new Properties();
        testProps1.setProperty("framework.credentials.file.encryption.key", "");
        FileOutputStream out1 = new FileOutputStream(testFile1);
        testProps1.store(out1, null);
        out1.close();
        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testFile1.toURI());

        File testFile2 = File.createTempFile("ejatfpf_2", ".properties");
        Properties testProps2 = new Properties();
        testProps2.setProperty("framework.secure.credentials.credsID.username", "testUsername");
        FileOutputStream out2 = new FileOutputStream(testFile2);
        testProps2.store(out2, null);
        out2.close();
        FileCredentialsStore fileCreds = new FileCredentialsStore(testFile2.toURI(), framework);

        FrameworkCredentialsStoreService test = new FrameworkCredentialsStoreService(framework, fpfcps, fileCreds, overrides);
        ICredentials creds = test.getCredentials("credsID");

        FileCredentialsUsername credsUsername = new FileCredentialsUsername("");
        if (creds instanceof FileCredentialsUsername) {
            credsUsername = (FileCredentialsUsername) creds;
        }

        assertEquals("Did not return expected class", "class io.ejat.framework.spi.creds.FileCredentialsUsername", creds.getClass().toString());
        assertEquals("Did not return expected value", "testUsername", credsUsername.getUsername());
    }


    /**
     * <p>This is a private class used to implement the IFramework for testing purposes.</p>
     */
    private class Framework implements IFramework{
        private Properties overrides = new Properties();
        private Properties records = new Properties();

        public IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace) throws ConfigurationPropertyStoreException {
            FrameworkConfigurationPropertyService fcps;
            try {
                File testFile = File.createTempFile("ejatfpf_", ".properties");
                Framework framework = new Framework();
                
                fcps = new FrameworkConfigurationPropertyService(framework, new FpfConfigurationPropertyStore(testFile.toURI()) , overrides, records, "framework");
                return fcps;
                
            } catch (Exception e) {
                System.out.println("Exception");
            }
            
            return null;  
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
        public ICredentialsStoreService getCredentialsService() throws CredentialsStoreException {
            return null;
        }
        
    } 

}