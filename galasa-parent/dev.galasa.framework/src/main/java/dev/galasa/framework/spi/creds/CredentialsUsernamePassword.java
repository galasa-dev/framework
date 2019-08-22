package dev.galasa.framework.spi.creds;

import java.io.UnsupportedEncodingException;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsUsernamePassword;

public class CredentialsUsernamePassword extends Credentials implements ICredentialsUsernamePassword {
	private String username;
	private String password;

	public CredentialsUsernamePassword(SecretKeySpec key, String username, String password) throws CredentialsException {
		super(key);

		try {
			this.username = new String(decode(username),"utf-8");
			this.password = new String(decode(password),"utf-8");
		} catch(UnsupportedEncodingException e) {
			throw new CredentialsException("utf-8 is not available for credentials", e);
		} catch (CredentialsException e) {
			throw e;
		}

	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}