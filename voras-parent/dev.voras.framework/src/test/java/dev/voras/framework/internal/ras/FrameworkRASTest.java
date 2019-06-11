package dev.voras.framework.internal.ras;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;

import dev.voras.framework.internal.ras.FrameworkResultArchiveStore;
import dev.voras.framework.spi.IResultArchiveStoreService;
import dev.voras.framework.spi.ResultArchiveStoreException;

/**
 * Small dummy little test, mainly to bump up code coverage
 *
 * @author Michael Baylis
 *
 */
public class FrameworkRASTest {

    @Test
    public void testBasicStuff() throws ResultArchiveStoreException {
        final IResultArchiveStoreService ras = mock(IResultArchiveStoreService.class);

        final FrameworkResultArchiveStore store = new FrameworkResultArchiveStore(null, ras);
        store.writeLog((String) null);
        store.writeLog((List<String>) null);
        store.updateTestStructure(null);
        store.getStoredArtifactsRoot();

        verify(ras).writeLog((String) null);
        verify(ras).writeLog((List<String>) null);
        verify(ras).updateTestStructure(null);
        verify(ras).getStoredArtifactsRoot();
    }

}
