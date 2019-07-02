package dev.voras;

public interface ICredentialsUsernameToken extends ICredentials {
	
	String getUsername();
	byte[] getToken();
    
}