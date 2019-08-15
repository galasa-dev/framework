package dev.galasa.mavenutils.plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Decrypt a credentials secure text string
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "decrypttext",
requiresProject=false)
public class DecryptText extends AbstractMojo{

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			//*** Get the encrypted string, must be prefixed with aes: (future proofing incase we get other encryption methods)
			System.out.println("Enter the text that is to be decrypted, including the aes: prefix");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String text = br.readLine().trim();
			if (text.isEmpty()) {
				System.out.println("No input entered, exiting");
				return;
			}
			
			if (!text.startsWith("aes:")) {
				System.out.println("Encrypted text does not start with aes:");
				return;
			}

			//*** Get the passphrase
			System.out.println("Enter the encryption key, WARNING, will be displayed on the screen");

			String passphrase = br.readLine().trim();
			if (passphrase.isEmpty()) {
				System.out.println("No input entered, exiting");
				return;
			}
			
			
			//** Break out the initialisation vector from the actual encrypted bytes
			byte[] decodedBytes = Base64.getDecoder().decode(text.substring(4).getBytes("utf-8"));
			byte[] iv = new byte[12];
			byte[] encrypted = new byte[decodedBytes.length - 12];
			System.arraycopy(decodedBytes, 0, iv, 0, 12);
			System.arraycopy(decodedBytes, 12, encrypted, 0, decodedBytes.length - 12);
			
			//*** Create the spec from the IV
			GCMParameterSpec paramSpec = new GCMParameterSpec(128, iv);
			
			//*** Convert the passphrase to a 256 bit hash
			byte[] key = passphrase.getBytes("utf-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			key = sha.digest(key);
			SecretKeySpec secretKeySpec =  new SecretKeySpec(key, "AES");
			
			//** Get the cipher
	        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
	        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
	        
	        //*** Decrypt the bytes
	        byte[] decrypted = cipher.doFinal(encrypted);

	        String sDecrypted = new String(decrypted, "utf-8");
	        
	        System.out.println("Decrypted string is:-");
	        System.out.println(sDecrypted);
			

		} catch(Exception e) {
			throw new MojoExecutionException("Error decrypting text", e);
		}
	}

}
