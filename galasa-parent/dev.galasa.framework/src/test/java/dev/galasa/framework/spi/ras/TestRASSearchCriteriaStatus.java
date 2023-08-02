/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.spi.ras; 

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.teststructure.TestStructure;


public class TestRASSearchCriteriaStatus {
    
    public TestStructure createMockTestStructure(String status){
			TestStructure testStructure = new TestStructure();
			testStructure.setRunName(RandomStringUtils.randomAlphanumeric(16));
			testStructure.setRequestor(RandomStringUtils.randomAlphanumeric(16));
			testStructure.setTestShortName(RandomStringUtils.randomAlphanumeric(16));
			testStructure.setBundle(RandomStringUtils.randomAlphanumeric(16));
			testStructure.setTestName(RandomStringUtils.randomAlphanumeric(16) + "." + RandomStringUtils.randomAlphanumeric(8));
			testStructure.setQueued(Instant.now().minus(1, ChronoUnit.HOURS).minus(30, ChronoUnit.MINUTES));
			testStructure.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS).minus(30, ChronoUnit.MINUTES));
			testStructure.setEndTime(Instant.now().minus(1, ChronoUnit.HOURS).minus(30, ChronoUnit.MINUTES));
            testStructure.setResult("Passed");
            testStructure.setStatus(status);
            return testStructure;
    }

    @Test
    public void TestGetDefaultStatusNames(){
        //Given ...
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.values());
        RasSearchCriteriaStatus searchCriteria = new RasSearchCriteriaStatus(statuses);
        //When ...
        List<TestRunLifecycleStatus> returnedStatuses = searchCriteria.getStatuses();
        //Then ...
        Assert.assertEquals(statuses,returnedStatuses);
    } 

    @Test
    public void TestCriteriaMatchedReturnsTrueWhenValidStatusInTestStructure(){
        //Given ...
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.values());
        RasSearchCriteriaStatus searchCriteria = new RasSearchCriteriaStatus(statuses);

        TestStructure testStructure = createMockTestStructure("running");
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
        Assert.assertEquals(criteriaMatched, true);
    } 

    @Test
    public void TestCriteriaMatchedReturnsFalseWhenInvalidStatusInTestStructure(){
        //Given ...
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.values());
        RasSearchCriteriaStatus searchCriteria = new RasSearchCriteriaStatus(statuses);

        TestStructure testStructure = createMockTestStructure("smthnWrong");
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
        Assert.assertEquals(criteriaMatched, false);
    }

    @Test
    public void TestCriteriaMatchedReturnsFalseWhenGivenNullStatusInTestStructure(){
        //Given ...
        List<TestRunLifecycleStatus> statuses = Arrays.asList(TestRunLifecycleStatus.values());
        RasSearchCriteriaStatus searchCriteria = new RasSearchCriteriaStatus(statuses);

        TestStructure testStructure = createMockTestStructure(null);
        //When ...
        boolean criteriaMatched = searchCriteria.criteriaMatched(testStructure);
        //Then ...
        Assert.assertEquals(criteriaMatched, false);
    } 
}