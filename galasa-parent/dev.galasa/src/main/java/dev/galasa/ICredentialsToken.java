/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa;

public interface ICredentialsToken extends ICredentials {

    byte[] getToken();

}
