/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasTestClass;

/**
 * 
 * @author Michael Baylis
 *
 */
public interface IResultArchiveStoreDirectoryService {

    @NotNull
    String getName();

    boolean isLocal();
    
    @NotNull
    List<IRunResult> getRuns(@NotNull IRasSearchCriteria... searchCriteria) throws ResultArchiveStoreException;

    /**
     * Get runs within the parameters specified
     * 
     * @param requestor who requested the run
     * @param from      UTC inclusive
     * @param to        UTC excluise
     * @return Run result
     * @throws ResultArchiveStoreException if there are errors accessing the RAS
     */

    @NotNull
    List<String> getRequestors() throws ResultArchiveStoreException;

    @NotNull
    List<RasTestClass> getTests() throws ResultArchiveStoreException;
    
    @NotNull
    List<String> getResultNames() throws ResultArchiveStoreException;
    

}
