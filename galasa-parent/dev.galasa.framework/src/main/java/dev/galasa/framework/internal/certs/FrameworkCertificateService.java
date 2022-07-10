package dev.galasa.framework.internal.certs;

import java.security.cert.X509Certificate;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.CertificateStoreException;
import dev.galasa.framework.spi.ICertificateStore;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IKeyStore;

public class FrameworkCertificateService implements ICertificateStoreService{
	
	public FrameworkCertificateService(ICertificateStore certStore) {
		
	}

	@Override
	public X509Certificate getX509Certificate(@NotNull String id) throws CertificateStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IKeyStore getDefaultKeystore() throws CertificateStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IKeyStore getKeystore(@NotNull String id) throws CertificateStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IKeyStore getKeyStore(@NotNull String... certificateId) throws CertificateStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IKeyStore getDefaultKeyStoreWithExtraCertificates(@NotNull String... certificateId)
			throws CertificateStoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
