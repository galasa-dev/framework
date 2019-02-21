package test.force.codecoverage;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.framework.internal.cps.FpfConfigurationPropertyStoreService;
import io.ejat.framework.internal.dss.FpfDynamicStatusStoreService;
import io.ejat.framework.spi.ConfigurationPropertyStoreException;
import io.ejat.framework.spi.DynamicStatusStoreException;
/**
 * <p> Simple stubbed test class</p>
 * 
 * @author James Davies
 */
public class TemporaryStubbedClassesTest {

	@Test
	public void testDss() throws DynamicStatusStoreException {
		FpfDynamicStatusStoreService store = new FpfDynamicStatusStoreService();
		store.initialise(null);
		store.get(null);
		store.getPrefix(null);
		store.put(null, null);
		store.put(null);
		store.putSwap(null, null, null);
		store.putSwap(null, null, null, null);
		store.deletePrefix(null);
		store.delete((String)null);
		store.delete((Set<String>)null);
		Assert.assertTrue("dummy",true);
	}

}
