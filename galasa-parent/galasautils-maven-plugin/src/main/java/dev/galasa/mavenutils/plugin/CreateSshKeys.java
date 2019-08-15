package dev.galasa.mavenutils.plugin;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.Base64;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;


/**
 * Encrypt a string of text for use in the Galasa Credentials service
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "createsshkey",
requiresProject=false)
public class CreateSshKeys extends AbstractMojo{

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			//*** Get the passphrase to be used
			System.out.println("Enter the encryption key, if required, WARNING, will be displayed on the screen");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String passphrase = br.readLine().trim();

			//*** Create the keypair
			JSch jsch=new JSch();
			KeyPair kpair=KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			kpair.writePublicKey(baos, "Galasa Generated Key");
			String publicKey = new String(baos.toByteArray(), "utf-8");

			baos = new ByteArrayOutputStream();
			if (!passphrase.isEmpty()) {
				kpair.writePrivateKey(baos, passphrase.getBytes());
			} else {
				kpair.writePrivateKey(baos);
			}
			String privateKey = new String(baos.toByteArray(), "utf-8");
			
			
			
			
			
			baos = new ByteArrayOutputStream();
			kpair.writePrivateKey(baos);
			String encodedPublicKey = null;
			if (!passphrase.isEmpty()) {
				encodedPublicKey = EncryptText.encrypt(baos.toByteArray(), passphrase);
			} else {
				encodedPublicKey = "base64:" + Base64.getEncoder().encodeToString(baos.toByteArray());
			}

			

			System.out.println("Public Key :-");
			System.out.println(publicKey);
			System.out.println("\nPrivate Key :-");
			System.out.println(privateKey);
			System.out.println("\nGalasa Credentials Token:-");
			System.out.println(encodedPublicKey);

		} catch(Exception e) {
			throw new MojoExecutionException("Error encrypting text", e);
		}
	}

}
