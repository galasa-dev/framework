package dev.voras.framework.internal.ras.directory;

import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IFrameworkInitialisation;
import dev.voras.framework.spi.IResultArchiveStoreRegistration;
import dev.voras.framework.spi.ResultArchiveStoreException;

/**
 * A RAS Registration
 *
 * @author Michael Baylis
 *
 */
@Component(service = { IResultArchiveStoreRegistration.class })
public class DirectoryResultArchiveStoreRegistration implements IResultArchiveStoreRegistration {

    private DirectoryResultArchiveStoreService service;
    
    private URI                            rasUri;
    /*
     * (non-Javadoc)
     *
     * @see
     * io.ejat.framework.spi.IResultArchiveStoreService#initialise(io.ejat.framework
     * .spi.IFrameworkInitialisation)
     */
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws ResultArchiveStoreException {
    	IFramework framework = frameworkInitialisation.getFramework();

        // *** See if this RAS is to be activated, will eventually allow multiples of
        // itself
        final List<URI> rasUris = frameworkInitialisation.getResultArchiveStoreUris();
        for (final URI uri : rasUris) {
            if ("file".equals(uri.getScheme())) {
                if (this.rasUri != null && !this.service.isShutdown()) {
                    throw new ResultArchiveStoreException(
                            "The Directory RAS currently does not support multiple instances of itself");
                }
                this.rasUri = uri;
            }
        }
        
        if (this.rasUri == null) {
        	return;
        }
        
        service = new DirectoryResultArchiveStoreService(framework, this.rasUri);
        frameworkInitialisation.registerResultArchiveStoreService(service);
        
        return;
    }

}
