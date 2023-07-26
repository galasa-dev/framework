/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework;

import java.util.*;

import org.apache.commons.lang3.EnumUtils;

public enum TestRunLifecycleStatus {
    FINISHED("finished"),
    BUILDING("building"),
    GENERATING("generating"),
    RUNNING("running"),
    RUNDONE("rundone"),
    UP("up"),
    STARTED("started"),
    PROVSTART("provstart"),
    ENDING("ending")
    ;

    private String value ;  // blame Mike (He coded this on a liveshare, what is value Mike???)

    private TestRunLifecycleStatus(String value) {
        this.value = value ;
    }
   

    public TestRunLifecycleStatus getStatusFromString(String statusInput) {
        return valueOf(statusInput);
    }

    public static boolean isStatusValid(String statusInput){
        return EnumUtils.isValidEnum(TestRunLifecycleStatus.class, statusInput);
    }

    // @Override
    // public String toString(){
    //     return value.toLowerCase();
    // }

/**
 * @return A string list from a TestRunLifecycleStatus list
 */
    public static List<String> convertTestRunLifecycleStausListToStringList (List<TestRunLifecycleStatus> testRunLifecycleStatuses){
        List<String> returnList = new ArrayList<String>();
        for (TestRunLifecycleStatus status : testRunLifecycleStatuses){
            returnList.add(status.value);
        }

        return returnList;
    }

      
    /** 
     * @return A list of possible status names, as strings
     */
    public static List<String> getAll() {
        List<String> validStatuses = new ArrayList<String>();
        for (TestRunLifecycleStatus status : TestRunLifecycleStatus.values()){
            validStatuses.add(status.toString());
        }
        return validStatuses;
    
    }
}
