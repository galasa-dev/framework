package dev.voras.framework.internal.creds;

import java.net.URI;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.voras.framework.spi.IFrameworkInitialisation;
import dev.voras.framework.spi.creds.CredentialsException;
import dev.voras.framework.spi.creds.ICredentialsStoreRegistration;

/**

 * 
 * @author Bruce Abbott
 */
@Component(service= {ICredentialsStoreRegistration.class})
public class FileCredentialsRegistration implements ICredentialsStoreRegistration {

	/**
	 * <p>This method registers this as the only Creds file.</p>
	 * 
	 * @param IFrameworkInitialisation
	 * @throws CredentialsStoreException
	 */
	@Override
	public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsException {
		try {
			URI creds = frameworkInitialisation.getCredentialsStoreUri();

			if (creds.getScheme().equals("file")) {
				FileCredentialsStore fcs = new FileCredentialsStore(creds, frameworkInitialisation.getFramework());
				frameworkInitialisation.registerCredentialsStore(fcs);
			}
		} catch (Exception e ) {
			throw new CredentialsException("Could not initialise Framework Property File CREDs", e);
		}
	}

}