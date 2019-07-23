package dev.voras.framework.api.launcher;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.voras.framework.FrameworkInitialisation;
import dev.voras.framework.spi.FrameworkException;
import dev.voras.framework.spi.IFramework;

@RunWith(MockitoJUnitRunner.class)
public class LauncherTest {

    @Mock
    FrameworkInitialisation mockedframeInit;

    @InjectMocks
    Launcher mockLauncher = spy(new Launcher());

    Map<String, Object> props = new HashMap<>();

    @Before
    public void setProperties() {
        props.clear();

        props.put("framework.bootstrap.url", "http://localhost:8181/bootstrap");
    }

    @Test
    public void testLauncher() throws Exception{
        boolean caught = false;

        IFramework mockedFramework = Mockito.mock(IFramework.class);
        
        doReturn(mockedframeInit).when(mockLauncher).init(any(Properties.class), any(Properties.class));
        
        when(mockedFramework.isInitialised()).thenReturn(true);
        when(mockedframeInit.getFramework()).thenReturn(mockedFramework);

        try {
            mockLauncher.activate(props);
        } catch (FrameworkException e) {
            caught = true;
        }
        assertFalse("Threw some exception", caught);
    }

}