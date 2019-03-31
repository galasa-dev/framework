package io.ejat.framework.internal.creds;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.creds.ICredentialsStoreService;
import io.ejat.framework.spi.creds.ICredentials;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.FrameworkPropertyFileException;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.creds.FileCredentialsToken;
import io.ejat.framework.spi.creds.FileCredentialsUsername;
import io.ejat.framework.spi.creds.FileCredentialsUsernamePassword;
import io.ejat.framework.spi.creds.ICredentialsStore;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Component(service= {ICredentialsStore.class})
public class FileCredentialsStore implements ICredentialsStore {
    private FrameworkPropertyFile fpf;
    private Boolean encrypted = false;
    private SecretKeySpec key;
    private IFramework framework;
    private IConfigurationPropertyStoreService cpsService;

    public FileCredentialsStore(URI file, IFramework framework) throws NoSuchAlgorithmException, ConfigurationPropertyStoreException {
        try {
            this.framework = framework;
            cpsService = this.framework.getConfigurationPropertyService("");
            fpf = new FrameworkPropertyFile(file);
            String encryptionKey = cpsService.getProperty("", "framework.credentials.file.encryption.key", "");
            if (encryptionKey != null) {
                encrypted = true;
                key = createKey(encryptionKey);
            }
        } catch (FrameworkPropertyFileException | UnsupportedEncodingException e) {
            fpf = null;
        }
    }

    @Override
    public ICredentials getCredentials(String credentialsId) throws CredentialsStoreException {
        String token = fpf.get("framework.secure.credentials." + credentialsId + ".token");
        String username = fpf.get("framework.secure.credentials." + credentialsId + ".username");
        String password = fpf.get("framework.secure.credentials." + credentialsId + ".password");
        try {
            if (encrypted) {
                password = decrypt(key, password);
            }
        } catch (Exception e) {
            
        }
        
        if (token != null) {
            return new FileCredentialsToken(token);        
        }
        else if (username != null) {
            if (fpf.get(password) != null) {
                return new FileCredentialsUsernamePassword(username, password);
            }
            else {
                return new FileCredentialsUsername(username);
            }
        }
        else {
            throw new CredentialsStoreException("Unable to find username");
        }
    }

    private static SecretKeySpec createKey(String secret) throws UnsupportedEncodingException, NoSuchAlgorithmException {	
		byte[] key = secret.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
		return new SecretKeySpec(key, "AES");
    }
    
    private static String decrypt(SecretKeySpec key, String encrypted) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
		return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
	}

}