/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.spi.ras; 

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.teststructure.TestStructure;


public class TestRASSearchCriteriaStatus {
    
    private TestStructure createMockTestStructure(String status){
			TestStructure testStructure = new TestStructure();
            testStructure.setStatus(status);
            return testStructure;
    }

    private RasSearchCriteriaStatus setupRasSearchCriteriaStatus(){
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.values());
        RasSearchCriteriaStatus searchCriteria = new RasSearchCriteriaStatus(statuses);
        return searchCriteria;
    }

    /* 
	*TESTS 
	*/
    
    @Test
    public void TestGetDefaultStatusNames(){
        //Given ...
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.values());
        RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();
        //When ...
        List<TestRunLifecycleStatus> returnedStatuses = searchCriteria.getStatuses();
        //Then ...
        Assert.assertEquals(statuses,returnedStatuses);
    } 

    @Test
    public void TestCriteriaMatchedReturnsTrueWhenValidStatusInTestStructure(){
        //Given ...
        RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();

        TestStructure testStructure = createMockTestStructure("running");
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
        Assert.assertEquals(criteriaMatched, true);
    } 

    @Test
    public void TestCriteriaMatchedReturnsFalseWhenInvalidStatusInTestStructure(){
        //Given ...
       RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();

        TestStructure testStructure = createMockTestStructure("smthnWrong");
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
        Assert.assertEquals(criteriaMatched, false);
    }

    @Test
    public void TestCriteriaMatchedReturnsFalseWhenGivenNullStatusInTestStructure(){
        //Given ...
        RasSearchCriteriaStatus searchCriteria = setupRasSearchCriteriaStatus();

        TestStructure testStructure = createMockTestStructure(null);
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
        Assert.assertEquals(criteriaMatched, false);
    } 
}