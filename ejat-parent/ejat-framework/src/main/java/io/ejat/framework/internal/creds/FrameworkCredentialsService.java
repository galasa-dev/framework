package io.ejat.framework.internal.creds;

import javax.crypto.IllegalBlockSizeException;
import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.FrameworkException;
import io.ejat.framework.spi.IConfidentialTextService;
import io.ejat.framework.spi.IConfigurationPropertyStoreService;
import io.ejat.framework.spi.IFramework;
import io.ejat.framework.spi.creds.CredentialsException;
import io.ejat.framework.spi.creds.ICredentials;
import io.ejat.framework.spi.creds.ICredentialsService;
import io.ejat.framework.spi.creds.ICredentialsStore;
import io.ejat.framework.spi.creds.ICredentialsToken;
import io.ejat.framework.spi.creds.ICredentialsUsernamePassword;

/**
 * <p>This class is used to drive the registered Credentials Store, and retireve values from the Credentials Store.</p>
 * 
 * @author Bruce Abbott
 * @author Michael Baylis
 */
public class FrameworkCredentialsService implements ICredentialsService {
	private final ICredentialsStore credsStore;
	private IConfidentialTextService confTextService;
	private boolean registerConfidentialText;

	/**
	 * <p>This constructor retrieves the location of stored credentials and registers credentials with the confidentials text store</p>
	 * 
	 * @param framework - The framework object
	 * @param credsStore - the registered store the the Credentials
	 * @throws FrameworkException 
	 */
	public FrameworkCredentialsService(IFramework framework, ICredentialsStore credsStore) throws CredentialsException {
		this.credsStore = credsStore;
		this.confTextService = framework.getConfidentialTextService();

		try {
			IConfigurationPropertyStoreService cpsService = framework.getConfigurationPropertyService("framework");
			this.registerConfidentialText = Boolean.parseBoolean(cpsService.getProperty("credentials", "auto.register.cts"));
		} catch(Exception e) {
			throw new CredentialsException("Unable to initialise the Credentials Service", e);
		}
	}

	/**
	 * <p>A simple method thta checks the provided URI to the CPS is a local file or not.</p>
	 * 
	 * @param credsId - id used to access the credentials
	 * @return - object containing appropriate credentials
	 * @throws CredentialsStoreException
	 * @throws IllegalBlockSizeException
	 */
	@Override
	public ICredentials getCredentials(@NotNull String credsId) throws CredentialsException {
		
		ICredentials creds;
		try {
			creds = this.credsStore.getCredentials(credsId);
		} catch (CredentialsException e) {
			throw new CredentialsException("Unable to retrieve credentials for id " + credsId, e);
		}
		if (creds == null) {
			return null;
		}
		
		if (!this.registerConfidentialText) {
			return creds;
		}
		
		if (creds instanceof ICredentialsToken) {
			ICredentialsToken token = (ICredentialsToken) creds;
			confTextService.registerText(token.getToken(), "Token for credentials id " + credsId);
			return creds;
		}
		
		if (creds instanceof ICredentialsUsernamePassword) {
			ICredentialsUsernamePassword up = (ICredentialsUsernamePassword) creds;
			confTextService.registerText(up.getPassword(), "Token for credentials id " + credsId);
			return creds;
		}
		
		return creds;
	}
}