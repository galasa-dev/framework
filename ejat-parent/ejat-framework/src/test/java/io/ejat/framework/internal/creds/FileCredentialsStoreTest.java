package io.ejat.framework.internal.creds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import io.ejat.framework.spi.IConfidentialTextService;
import io.ejat.framework.internal.cps.FpfConfigurationPropertyStore;
import io.ejat.framework.internal.cps.FrameworkConfigurationPropertyService;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IDynamicStatusStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
import io.ejat.framework.spi.creds.ICredentialsStoreService;
import io.ejat.framework.spi.IResourcePoolingService;
import io.ejat.framework.spi.IResultArchiveStore;
import io.ejat.framework.spi.creds.ICredentials;
import io.ejat.framework.spi.creds.FileCredentialsToken;
import io.ejat.framework.spi.creds.FileCredentialsUsernamePassword;
import io.ejat.framework.spi.creds.FileCredentialsUsername;

import java.security.NoSuchAlgorithmException;

/**
 * <p>This test class checks the behaviour of retrieving credentials from the local store is functional.</p>
 * 
 * @author Bruce Abbott
 */
public class FileCredentialsStoreTest {

    /**
     * <p>This test method checks that a local Credentials Store can be constructed.</p>
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws ConfigurationPropertyStoreException
     */
    @Test
    public void testConstructor() throws IOException, NoSuchAlgorithmException, ConfigurationPropertyStoreException {
        File testProp = File.createTempFile("ejatfpf_", ".properties");
        Framework framework = new Framework();

        FileCredentialsStore fileCreds = new FileCredentialsStore(testProp.toURI(), framework);
        assertTrue("Dummy", true);
    }

    /**
     * <p>This test method checks that a token stored in the local Credentials Store can be retrieved.</p>
     * @throws IOException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     * @throws ConfigurationPropertyStoreException
     * @throws CredentialsStoreException
     */
    @Test
    public void testGetCredentialsToken() throws IOException, FileNotFoundException, NoSuchAlgorithmException, ConfigurationPropertyStoreException, CredentialsStoreException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        Properties testProps = new Properties();
        testProps.setProperty("framework.credentials.file.encryption.key", "");
        testProps.setProperty("framework.secure.credentials.credsID.token", "testToken");
        FileOutputStream out = new FileOutputStream(testFile);
        testProps.store(out, null);
        out.close();

        Framework framework = new Framework();

        FileCredentialsStore fileCreds = new FileCredentialsStore(testFile.toURI(), framework);
        ICredentials creds = fileCreds.getCredentials("credsID");

        FileCredentialsToken credsToken = new FileCredentialsToken("");
        if (creds instanceof FileCredentialsToken) {
            credsToken = (FileCredentialsToken) creds;
        }

        assertEquals("Did not return expected class", "class io.ejat.framework.spi.creds.FileCredentialsToken", creds.getClass().toString());
        assertEquals("Did not return expected value", "testToken", credsToken.getToken());
    }

    /**
     * <p>This test method checks that a username/password stored in the local Credentials Store can be retrieved.</p>
     * @throws IOException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     * @throws ConfigurationPropertyStoreException
     * @throws CredentialsStoreException
     */
    @Test
    public void testGetCredentialsUsernamePassword() throws IOException, FileNotFoundException, NoSuchAlgorithmException, ConfigurationPropertyStoreException, CredentialsStoreException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        Properties testProps = new Properties();
        testProps.setProperty("framework.credentials.file.encryption.key", "");
        testProps.setProperty("framework.secure.credentials.credsID.username", "testUsername");
        testProps.setProperty("framework.secure.credentials.credsID.password", "testPassword");
        FileOutputStream out = new FileOutputStream(testFile);
        testProps.store(out, null);
        out.close();

        Framework framework = new Framework();

        FileCredentialsStore fileCreds = new FileCredentialsStore(testFile.toURI(), framework);
        ICredentials creds = fileCreds.getCredentials("credsID");

        FileCredentialsUsernamePassword credsUsernamePassword = new FileCredentialsUsernamePassword("", "");
        if (creds instanceof FileCredentialsUsernamePassword) {
            credsUsernamePassword = (FileCredentialsUsernamePassword) creds;
        }

        assertEquals("Did not return expected class", "class io.ejat.framework.spi.creds.FileCredentialsUsernamePassword", creds.getClass().toString());
        assertEquals("Did not return expected value", "testUsername", credsUsernamePassword.getUsername());
        assertEquals("Did not return expected value", "testPassword", credsUsernamePassword.getPassword());
    }

    /**
     * <p>This test method checks that a username stored in the local Credentials Store can be retrieved.</p>
     * @throws IOException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     * @throws ConfigurationPropertyStoreException
     * @throws CredentialsStoreException
     */
    @Test
    public void testGetCredentialsUsername() throws IOException, FileNotFoundException, NoSuchAlgorithmException, ConfigurationPropertyStoreException, CredentialsStoreException {
        File testFile = File.createTempFile("ejatfpf_", ".properties");
        Properties testProps = new Properties();
        testProps.setProperty("framework.credentials.file.encryption.key", "");
        testProps.setProperty("framework.secure.credentials.credsID.username", "testUsername");
        FileOutputStream out = new FileOutputStream(testFile);
        testProps.store(out, null);
        out.close();

        Framework framework = new Framework();

        FileCredentialsStore fileCreds = new FileCredentialsStore(testFile.toURI(), framework);
        ICredentials creds = fileCreds.getCredentials("credsID");

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