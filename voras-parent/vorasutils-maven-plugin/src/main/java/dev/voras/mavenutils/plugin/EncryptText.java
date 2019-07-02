package dev.voras.mavenutils.plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Encrypt a string of text for use in the Voras Credentials service
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "encrypttext",
requiresProject=false)
public class EncryptText extends AbstractMojo{

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			//*** Get the text to be encrypted
			System.out.println("Enter the text that is to be encrypted, WARNING, will be displayed on the screen");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String text = br.readLine().trim();
			if (text.isEmpty()) {
				System.out.println("No input entered, exiting");
				return;
			}

			//*** Get the passphrase to be used
			System.out.println("Enter the encryption key, WARNING, will be displayed on the screen");

			String passphrase = br.readLine().trim();
			if (passphrase.isEmpty()) {
				System.out.println("No input entered, exiting");
				return;
			}
			
	        
	        System.out.println("Encrypted string for use in credentials is:-");
	        System.out.println("aes:" + encrypt(text.getBytes(), passphrase));
			

		} catch(Exception e) {
			throw new MojoExecutionException("Error encrypting text", e);
		}
	}
	
	
	public static String encrypt(byte[] text, String passphrase) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		//*** Geneate an initialisation vector,  kind of like a salt,  random 12 bytes			
		SecureRandom sr = new SecureRandom();
		byte[] iv = new byte[12];
		sr.nextBytes(iv);
		GCMParameterSpec paramSpec = new GCMParameterSpec(128, iv);
		
		//*** Convert the passphrase to a 256 bit hash
		byte[] key = passphrase.getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		key = sha.digest(key);
		SecretKeySpec secretKeySpec =  new SecretKeySpec(key, "AES");
		
		//*** Get the Cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
        
        //*** Encrypt the data
        byte[] encrypted = cipher.doFinal(text);
        
        //*** Add the IV to the front of the encrypted data for decryption
        byte[] ivEncrypted = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, ivEncrypted, 0, iv.length);
        System.arraycopy(encrypted, 0, ivEncrypted, 12, encrypted.length);
        
        //*** Base64 encode it
        return Base64.getEncoder().encodeToString(ivEncrypted);
	}

}
