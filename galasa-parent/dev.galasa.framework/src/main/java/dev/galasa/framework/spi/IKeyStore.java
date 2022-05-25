/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.spi;

/**
 * A class to represent a Java Keystore and allow for easy appending and generation
 * 
 * @author jamesdavies
 *
 */
public interface IKeyStore {
	/**
	 * <p>
	 * Allows a Base64 encoded PEM cert to be appended to the Keystore
	 * </p>
	 * 
	 * @param cert
	 * @throws CertificateStoreException
	 */
	public void appendPem(String cert) throws CertificateStoreException;
	
	/**
	 * <p>
	 * Allows a Base64 encoded DER cert to be appended to the Keystore
	 * </p>
	 * 
	 * @param cert
	 * @throws CertificateStoreException
	 */
	public void appendDer(String cert) throws CertificateStoreException;
	
	/**
	 * <p>
	 * Allows a certificate to be retrieved from the the certificate store based on its
	 * ID tag and added to this keystore.
	 * </p>
	 * 
	 * @param CertificateId
	 * @throws CertificateStoreException
	 */
	public void appendCertficate(String certificateId) throws CertificateStoreException;

}
