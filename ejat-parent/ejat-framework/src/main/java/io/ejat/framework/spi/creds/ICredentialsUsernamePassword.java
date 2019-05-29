package io.ejat.framework.spi.creds;

public interface ICredentialsUsernamePassword extends ICredentials {
	
	String getUsername();
	String getPassword();
    
}