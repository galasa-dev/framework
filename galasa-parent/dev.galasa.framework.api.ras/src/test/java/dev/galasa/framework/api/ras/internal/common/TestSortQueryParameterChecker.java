/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.common;

import static org.assertj.core.api.Assertions.*;
import dev.galasa.framework.api.ras.internal.verycommon.*;
import dev.galasa.framework.api.ras.internal.verycommon.QueryParameters;

import org.junit.Test;
import java.util.*;


public class TestSortQueryParameterChecker {


    @Test
    public void testSortFieldAscendingReturnsTrue() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {"runname:asc"} );
        QueryParameters params = new QueryParameters(map);
        SortQueryParameterChecker checker = new SortQueryParameterChecker();


        boolean isAscending = checker.isAscending(params, "runname");
        
        assertThat(isAscending).isTrue();
    }

    @Test
    public void testSortFieldNoAscOrDescValueThrowsInvalidValueError() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {"runname:"} );
        QueryParameters params = new QueryParameters(map);
        SortQueryParameterChecker checker = new SortQueryParameterChecker();

        Throwable thrown = catchThrowable( () -> { 
            checker.isAscending(params, "runname"); 
        });

        assertThat(thrown.getMessage()).contains("GAL5011","runname","sort");
    }

    @Test
    public void testSortFieldManySortValuePartsThrowsInvalidValueError() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {"runname:asc:desc:asc"} );
        QueryParameters params = new QueryParameters(map);
        SortQueryParameterChecker checker = new SortQueryParameterChecker();

        Throwable thrown = catchThrowable( () -> { 
            checker.isAscending(params, "runname"); 
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5011","runname:asc:desc:asc","sort");
    }

    @Test
    public void testSortFieldNoQueryDefaultFalse() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        QueryParameters params = new QueryParameters(map);
        SortQueryParameterChecker checker = new SortQueryParameterChecker();

        boolean isAscending = checker.isAscending(params, "runname");
        
        assertThat(isAscending).isFalse();
    }

    @Test
    public void testSortFieldNoValueReturnsDefaultFalse() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {""} );
        QueryParameters params = new QueryParameters(map);
        SortQueryParameterChecker checker = new SortQueryParameterChecker();

        boolean isAscending = checker.isAscending(params, "runname");
        
        assertThat(isAscending).isFalse();
    }

    @Test
    public void testSortFieldNoValueReturnsDefaultFalseValidate() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {""} );
        QueryParameters params = new QueryParameters(map);
        SortQueryParameterChecker checker = new SortQueryParameterChecker();

        boolean validateSortValue = checker.validateSortValue(params);
        
        assertThat(validateSortValue).isFalse();
    }

    @Test
    public void testSortFieldValueReturnsTrueValidate() throws Exception {
        Map<String,String[]> map = new HashMap<String,String[]>();
        map.put("sort", new String[] {"to:asc"} );
        QueryParameters params = new QueryParameters(map);
        SortQueryParameterChecker checker = new SortQueryParameterChecker();

        boolean validateSortValue = checker.validateSortValue(params);
        
        assertThat(validateSortValue).isTrue();
    }
}