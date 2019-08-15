package dev.galasa;

public interface ICredentialsUsernameToken extends ICredentials {
	
	String getUsername();
	byte[] getToken();
    
}