package io.ejat.framework.spi;

/**
 * <p>Used by the eJAT Framework to initialise the various Dynamic Status Stores that may exist within the OSGi instance.  Only 1 DSS maybe enabled during the lifetime of 
 * a eJAT test run or server instance.</p>
 * 
 * <p>The DSS should request from the framework the URI that is defined in the DSS.  It should examine the returned URI to 
 * determine if it is this DSS that is required to be initialised.  If the DSS should be initialised, the DSS should do so 
 * and then register itself in the Framework.</p>
 *  
 * @author Michael Baylis
 *
 */
public interface IDynamicStatusStore extends IDynamicStatusStoreKeyAccess {
		
}
