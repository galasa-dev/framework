/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.teststructure.TestStructure;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.framework.api.ras.internal.mocks.*;
import java.util.*;

import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class TestTestClassesRoute extends RasServletTest{

    final static Gson gson = GalasaGsonBuilder.build();

    public List<IRunResult> generateTestData (int resSize){
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		// Build the results the DB will return.
		for(int c =0 ; c < resSize; c++){
			String runId = RandomStringUtils.randomAlphanumeric(16);
			TestStructure testStructure = new TestStructure();
			switch (c % 5){
                case 0: testStructure.setRequestor("galasa");
					break;
				case 1: testStructure.setRequestor("mickey");
					break;
				case 2: testStructure.setRequestor("user");
					break;
                case 3: testStructure.setRequestor("UNKNOWN");
					break;
                case 4: testStructure.setRequestor("jindex");
					break;
			}
			IRunResult result = new MockRunResult( runId, testStructure, null , null);
			mockInputRunResults.add(result);
		}
		return mockInputRunResults;
	}

    private String generateExpectedJSON (List<IRunResult> mockInputRunResults, boolean reverse) throws ResultArchiveStoreException{

        HashMap<String,RasTestClass> tests = new HashMap<>();
        String key;
        for (IRunResult run : mockInputRunResults){
			TestStructure testStructure = run.getTestStructure();
			key = testStructure.getBundle()+"/"+testStructure.getTestName();
			if(!tests.containsKey(key)){
				tests.put(key,new RasTestClass(testStructure.getTestName(), testStructure.getBundle()));
			}
        }
        List<RasTestClass> testClasses = new ArrayList<>(tests.values());
        
        testClasses.sort(Comparator.comparing(RasTestClass::getTestClass));
        if (reverse == true) {
            testClasses.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
        }
		JsonElement jsonResultsArray = new Gson().toJsonTree(testClasses);
		JsonObject json = new JsonObject();
		json.add("testclasses", jsonResultsArray);
		return json.toString();
    }

    /*
     * Tests 
     */

}