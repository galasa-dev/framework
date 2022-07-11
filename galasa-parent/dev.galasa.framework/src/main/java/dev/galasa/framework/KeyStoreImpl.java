package dev.galasa.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import dev.galasa.framework.spi.CertificateStoreException;
import dev.galasa.framework.spi.ICertificateStore;
import dev.galasa.framework.spi.IKeyStore;

public class KeyStoreImpl implements IKeyStore {
	private KeyStore ks;
	private CertificateFactory certFact;
	private ICertificateStore certStore;
	
	public KeyStoreImpl(ICertificateStore certStore) throws KeyStoreException {
		this.certStore = certStore;
		try {
			ks = KeyStore.getInstance("JKS");
			ks.load(null);
			
			certFact = CertificateFactory.getInstance("X.509");
		} catch (NoSuchAlgorithmException | CertificateException |IOException e) {
			throw new KeyStoreException("Failed to create keystore", e);
		}
	}

	@Override
	public KeyStore getKeyStore() {
		return this.ks;
	}

	@Override
	public void appendPemAsString(String aliasId, String cert) throws CertificateStoreException {
		try {
			X509Certificate certificate = (X509Certificate) certFact.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
			ks.setCertificateEntry(aliasId, certificate);
		} catch (KeyStoreException | CertificateException e) {
			throw new CertificateStoreException("Failed to append certifacte.", e);
		}
	}

	/**
	 * This will append any DER-encoded certificate to the keystore. The file can either be a binary or a base64 encoded string format.
	 * 
	 * @param aliasId
	 * @param certFile
	 * @throws CertificateStoreException
	 */
	@Override
	public void appendCertificate(String aliasId, InputStream certFile) throws CertificateStoreException {
		try {
			X509Certificate certificate = (X509Certificate) certFact.generateCertificate(certFile);
			ks.setCertificateEntry(aliasId, certificate);
		} catch (KeyStoreException | CertificateException e) {
			throw new CertificateStoreException("Failed to append certifacte.", e);
		}
	}

	@Override
	public void appendCertficateById(String aliasId, String certificateId) throws CertificateStoreException {
		try {
			ks.setCertificateEntry(aliasId, certStore.getX509Certificate(certificateId));
		} catch (KeyStoreException | CertificateStoreException e) {
			throw new CertificateStoreException("Failed to append certifiacte from id",e);
		}
		
	}

}
