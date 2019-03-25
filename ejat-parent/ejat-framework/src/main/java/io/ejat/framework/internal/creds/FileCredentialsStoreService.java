package io.ejat.framework.internal.creds;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.osgi.service.component.annotations.Component;

import io.ejat.framework.spi.creds.ICredentialsStoreService;
import io.ejat.framework.spi.creds.ICredentials;
import io.ejat.framework.spi.FrameworkPropertyFile;
import io.ejat.framework.spi.FrameworkPropertyFileException;
import io.ejat.framework.spi.creds.CredentialsStoreException;
import io.ejat.framework.spi.IFrameworkInitialisation;
import io.ejat.framework.spi.creds.FileCredentialsToken;
import io.ejat.framework.spi.creds.FileCredentialsUsername;
import io.ejat.framework.spi.creds.FileCredentialsUsernamePassword;

@Component(service= {ICredentialsStoreService.class})
public class FileCredentialsStoreService implements ICredentialsStoreService {
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

    @Override
    public ICredentials getCredentials(String credentialsId) throws CredentialsStoreException {
        String token = fpf.get("framework.secure.credentials." + credentialsId + ".token");
        String username = fpf.get("framework.secure.credentials." + credentialsId + ".username");
        String password = fpf.get("framework.secure.credentials." + credentialsId + ".password");
        if (!token.equals(null)) {
            return new FileCredentialsToken(token);        
        }
        else if (!username.equals(null)) {
            if (!fpf.get(password).equals(null)) {
                return new FileCredentialsUsernamePassword(username, password);
            }
            else {
                return new FileCredentialsUsername(username);
            }
        }
        else {
            throw new CredentialsStoreException("Unable to find username");
        }
    }
}