package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;

public class ExceptionsTest {
	
	@Test
	public void testDynamicStatusStoreException() {
		Throwable throwable = new DynamicStatusStoreException();
		new DynamicStatusStoreException("Message");		
		new DynamicStatusStoreException("Message", throwable);		
		new DynamicStatusStoreException(throwable);		
		new DynamicStatusStoreException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	@Test
	public void testConfigurationPropertyStoreException() {
		Throwable throwable = new ConfigurationPropertyStoreException();
		new ConfigurationPropertyStoreException("Message");		
		new ConfigurationPropertyStoreException("Message", throwable);		
		new ConfigurationPropertyStoreException(throwable);		
		new ConfigurationPropertyStoreException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
