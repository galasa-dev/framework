package io.ejat.framework.internal.creds;

import java.net.URI;

import org.osgi.service.component.annotations.Component;

import javax.validation.constraints.NotNull;

import io.ejat.framework.spi.creds.ICredentialsStoreRegistration;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.internal.creds.FileCredentialsStore;

/**

 * 
 * @author Bruce Abbott
 */
@Component(service= {ICredentialsStoreRegistration.class})
public class FileCredentialsRegistration implements ICredentialsStoreRegistration {
    private FrameworkPropertyFile fpf;

    /**
	 * <p>This method registers this as the only Creds file.</p>
	 * 
	 * @param IFrameworkInitialisation
	 * @throws CredentialsStoreException
	 */
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsStoreException {
        try {
            URI creds = frameworkInitialisation.getCredentialsStoreUri();
            fpf = new FrameworkPropertyFile(creds);
            FileCredentialsStore fcs = new FileCredentialsStore(creds, frameworkInitialisation.getFramework());
            frameworkInitialisation.registerCredentialsStore(fcs);
        } catch (Exception e ) {
            throw new CredentialsStoreException("Could not initialise Framework Property File", e);
        }
    }

}