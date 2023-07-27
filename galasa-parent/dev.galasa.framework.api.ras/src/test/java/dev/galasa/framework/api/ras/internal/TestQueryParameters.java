/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal;

import java.time.Instant;
import static javax.servlet.http.HttpServletResponse.*;
import java.util.*;

import org.junit.Test;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.ras.internal.verycommon.QueryParameters;

import static org.assertj.core.api.Assertions.*;


public class TestQueryParameters extends RasServletTest {


    //-----------------------------------------------------------------
    // When the query parameter is a List of Strings
    //-----------------------------------------------------------------
    @Test
    public void testGetMultipleStringIfMultipleStringPresentOK() throws Exception {
        // Given...
        String[] inputList = new String[] { "elf,orc,human" };
        Map<String,String[]> inputs = new HashMap<>();
        inputs.put("race", inputList);
        QueryParameters params = new QueryParameters(inputs);

        // When...
        List<String> gotBack = params.getMultipleString("race", Arrays.asList("elf"));

        // Then...
        assertThat(gotBack).isEqualTo(Arrays.asList("elf", "orc", "human"));
    }

    @Test
    public void testGetMultipleStringIfMultipleStringNotPresentReturnsDefault() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<>();
        QueryParameters params = new QueryParameters(inputs);

        // When...
        List<String> gotBack = params.getMultipleString("race", Arrays.asList("orc", "dragon"));

        // Then...
        assertThat(gotBack).isEqualTo(Arrays.asList("orc", "dragon"));
    }

    //-----------------------------------------------------------------
    // When the query parameter is a Single String
    //-----------------------------------------------------------------
    @Test
    public void testGetSingleStringIfSingleStringPresentOK() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<>();
        inputs.put("race",new String[] { "elf" });
        QueryParameters params = new QueryParameters(inputs);

        // When...
        String gotBack = params.getSingleString("race", "elf");

        // Then...
        assertThat(gotBack).isEqualTo("elf");
    }

    @Test
    public void testGetSingleStringIfSingleStringHasSpacePaddingGetsTrimmed() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<>();
        inputs.put("race",new String[] { " elf " });
        QueryParameters params = new QueryParameters(inputs);

        // When...
        String gotBack = params.getSingleString("race", "elf");

        // Then...
        assertThat(gotBack).isEqualTo("elf");
    }

    @Test
    public void testGetSingleStringIfSingleStringNotPresentReturnsDefault() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<>();
        QueryParameters params = new QueryParameters(inputs);

        // When...
        String gotBack = params.getSingleString("race", "orc");

        // Then...
        assertThat(gotBack).isEqualTo("orc");
    }

    @Test
    public void testGetSingleStringIfMultipleInstancesOfThatParameterPresentThrowsDuplicateException() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<String,String[]>();
        QueryParameters params = new QueryParameters(inputs);
        inputs.put("race", new String[]{"orc","astari","hobbit"} );

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getSingleString("race", "default");
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("race")
            .hasMessageContaining("GAL5006") // GAL5006_INVALID_QUERY_PARAM_DUPLICATES
            ;
    }

    //-----------------------------------------------------------------
    // When the query parameter is an integer
    //-----------------------------------------------------------------
    @Test
    public void testGetSingleIntIfSingleStringPresentOK() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<>();
        inputs.put("dex",new String[] { "16" });
        QueryParameters params = new QueryParameters(inputs);

        // When...
        String gotBack = params.getSingleString("dex", "9");

        // Then...
        assertThat(gotBack).isEqualTo("16");
    }

    @Test
    public void testGetSingleIntWhenPropertyMissingReturnsDefaultValue() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<String,String[]>();
        QueryParameters params = new QueryParameters(inputs);

        // When...
        int gotBack = params.getSingleInt("runId", 100);

        // Then...
        assertThat(gotBack).isEqualTo(100);
    }


    @Test
    public void testGetSingleIntIfMultipleInstancesOfThatParameterPresentThrowsDuplicateException() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<String,String[]>();
        QueryParameters params = new QueryParameters(inputs);
        inputs.put("runId", new String[]{"1","2","3"} );

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getSingleInt("runId", 64);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("runId")
            .hasMessageContaining("GAL5006") // GAL5006_INVALID_QUERY_PARAM_DUPLICATES
        ;
    }

    @Test
    public void testGetSingleIntIfSingleInstancesOfThatParameterIsNotANumberThrowsException() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<String,String[]>();
        QueryParameters params = new QueryParameters(inputs);
        inputs.put("runId", new String[]{" notANumber "} );

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getSingleInt("runId", 64);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("runId")
            .hasMessageContaining("GAL5005") // GAL5005_INVALID_QUERY_PARAM_NOT_INTEGER
        ;
    }

    //-----------------------------------------------------------------
    // When the query parameter is a DateTime Instance
    //-----------------------------------------------------------------
    @Test
    public void testGetSingleDateTimeIfSingleDateTimePresentOK() throws Exception {
        // Given...
        Instant nowInstant = Instant.now();
        String nowString = nowInstant.toString();

        Map<String,String[]> inputs = new HashMap<>();

        inputs.put("born",new String[] { nowString });
        QueryParameters params = new QueryParameters(inputs);


        // When...
        Instant gotBack = params.getSingleInstant("born", null );

        // Then...
        assertThat(gotBack).isEqualTo(nowInstant);
    }

    @Test
    public void testGetSingleInstantIfSingleStringHasSpacePaddingGetsTrimmed() throws Exception {
        // Given...
        Instant nowInstant = Instant.now();
        String nowString = nowInstant.toString();

        Map<String,String[]> inputs = new HashMap<>();

        inputs.put("born",new String[] { nowString });
        QueryParameters params = new QueryParameters(inputs);

        // When...
        Instant gotBack = params.getSingleInstant("born", null);

        // Then...
        assertThat(gotBack).isEqualTo(nowInstant);
    }

    @Test
    public void testGetSingleInstantIfSingleParamNotPresentReturnsDefault() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<>();
        QueryParameters params = new QueryParameters(inputs);

        // When...
        Instant gotBack = params.getSingleInstant("born", null);

        // Then...
        assertThat(gotBack).isNull();
    }

    @Test
    public void testGetSingleInstantIfMultipleInstancesOfThatParameterPresentThrowsDuplicateException() throws Exception {
        // Given...
        Instant nowInstant = Instant.now();
        String nowString = nowInstant.toString();

        Map<String,String[]> inputs = new HashMap<>();

        inputs.put("born",new String[] { nowString , nowString });
        QueryParameters params = new QueryParameters(inputs);

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getSingleInstant("born", null);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("born")
            .hasMessageContaining("GAL5006") // GAL5006_INVALID_QUERY_PARAM_DUPLICATES
            ;
        assertThat(( (InternalServletException)thrown).getHttpFailureCode()).isEqualTo(SC_BAD_REQUEST);
    }

    @Test
    public void testGetSingleInstantIfSingleInstancesOfThatParameterIsNotADateTimeThrowsException() throws Exception {
        // Given...
        Map<String,String[]> inputs = new HashMap<String,String[]>();
        Instant nowInstant = Instant.now();

        QueryParameters params = new QueryParameters(inputs);
        inputs.put("died", new String[]{"notAValidDateTime"} );

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getSingleInstant("died", nowInstant);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("died")
            .hasMessageContaining("GAL5001") // GAL5001_INVALID_DATE_TIME_FIELD
        ;
    }

    //-----------------------------------------------------------------
    // Test getResultsFromParameters
    //-----------------------------------------------------------------

    @Test
    public void testGetResultsFromParametersIfMultipleResultLowerCaseParametersOK() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "passed,failed,envfail" });
        QueryParameters params = new QueryParameters(mockURLQuery);

        // When...
        List<String> returnedResults = params.getResultsFromParameters(mockRasResultNames);

        // Then...
        assertThat(returnedResults).isEqualTo(Arrays.asList("Passed", "Failed", "EnvFail"));
    }

    @Test
    public void testGetResultsFromParametersIfMultipleResultUpperCaseParametersOK() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "PASSED,FAILED,ENVFAIL" });
        QueryParameters params = new QueryParameters(mockURLQuery);

        // When...
        List<String> returnedResults = params.getResultsFromParameters(mockRasResultNames);

        // Then...
        assertThat(returnedResults).isEqualTo(Arrays.asList("Passed", "Failed", "EnvFail"));
    }

    @Test
    public void testGetResultsFromParametersIfMultipleResultMixedCaseParametersOK() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "pasSed,Failed,envfAIl" });
        QueryParameters params = new QueryParameters(mockURLQuery);

        // When...
        List<String> returnedResults = params.getResultsFromParameters(mockRasResultNames);

        // Then...
        assertThat(returnedResults).isEqualTo(Arrays.asList("Passed", "Failed", "EnvFail"));
    }

    @Test
    public void testGetResultsFromParametersIfResultParameterIsInvalidThrowsException() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<String,String[]>();
        QueryParameters params = new QueryParameters(mockURLQuery);
        mockURLQuery.put("result", new String[]{"Passed,Failed,garbage"} );

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getResultsFromParameters(mockRasResultNames);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("garbage")
            .hasMessageContaining("GAL5013"); // GAL5013_RESULT_NAME_NOT_RECOGNIZED

    }

    @Test
    public void testGetResultsFromParametersIfResultParameterIsNullReturnsNull() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<String,String[]>();
        QueryParameters params = new QueryParameters(mockURLQuery);
        mockURLQuery.put("result", new String[]{} );

        // When...

        List<String> returnedResults = params.getResultsFromParameters(mockRasResultNames);


        // Then...
        assertThat(returnedResults)
            .isNull();

    }

    @Test
    public void testGetResultsFromParametersWithMultipleCommasTogetherReturnsError() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "pasSed,,Failed,env,fAIl" });
        QueryParameters params = new QueryParameters(mockURLQuery);

        // When...
        Throwable thrown = catchThrowable( () -> {
            params.getResultsFromParameters(mockRasResultNames);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("''")
            .hasMessageContaining("GAL5013"); // GAL5013_RESULT_NAME_NOT_RECOGNIZED
    }

    @Test
    public void testGetResultsFromParametersWithMultipleCommasInParameterReturnsError() throws Exception {
        // Given...
        List<String> mockRasResultNames = new ArrayList<String>();
        mockRasResultNames.add("Passed");
        mockRasResultNames.add("Failed");
        mockRasResultNames.add("EnvFail");
        mockRasResultNames.add("UNKNOWN");
        Map<String,String[]> mockURLQuery = new HashMap<>();
        mockURLQuery.put("result", new String[] { "pasSed,Failed,env,fAIl" });
        QueryParameters params = new QueryParameters(mockURLQuery);

        /// When...
        Throwable thrown = catchThrowable( () -> {
            params.getResultsFromParameters(mockRasResultNames);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("env")
            .hasMessageContaining("GAL5013"); // GAL5013_RESULT_NAME_NOT_RECOGNIZED
    }
}
