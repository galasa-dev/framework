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
import io.ejat.framework.spi.creds.FileCredentialsToken;
import io.ejat.framework.spi.creds.FileCredentialsUsername;
import io.ejat.framework.spi.creds.FileCredentialsUsernamePassword;
import io.ejat.framework.spi.creds.ICredentialsStore;

@Component(service= {ICredentialsStoreService.class})
public class FileCredentialsStore implements ICredentialsStore {
    private FrameworkPropertyFile fpf;

    public FileCredentialsStore(URI file) {
        try {
            fpf = new FrameworkPropertyFile(file);
        } catch (FrameworkPropertyFileException e) {
            fpf = null;
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