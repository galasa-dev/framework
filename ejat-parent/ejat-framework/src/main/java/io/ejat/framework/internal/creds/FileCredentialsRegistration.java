package io.ejat.framework.internal.creds;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.creds.ICredentialsRegistration;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.FrameworkPropertyFileException;
import io.ejat.framework.spi.creds.CredentialsStoreException;

@Component(service= {ICredentialsRegistration.class})
public class FileCredentialsRegistration implements ICredentialsRegistration {
    private FrameworkPropertyFile fpf;

    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsStoreException {
        try {
            //Not sure what the URI should be
            URI creds = new URI("framework");
            fpf = new FrameworkPropertyFile(creds);
            frameworkInitialisation.registerCredentialsStoreService(this);
        } catch (FrameworkPropertyFileException | URISyntaxException e ) {
            throw new CredentialsStoreException("Could not initialise Framework Property File", e);
        }
    }

}