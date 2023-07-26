/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework;

import java.util.*;

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

    private String value ;

    private TestRunLifecycleStatus(String value) {
        this.value = value ;
    }
   
    public String toString() {
        return value;
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
