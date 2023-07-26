/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaStatus implements IRasSearchCriteria {
   
   private final TestRunLifecycleStatus[] statuses;
   
   public RasSearchCriteriaStatus(@NotNull TestRunLifecycleStatus... statuses) {
      this.statuses = statuses;
   }
   
   @Override
   public boolean criteriaMatched(@NotNull TestStructure structure) {
      
      if(structure == null) {
         return false;
      }
      
      if(statuses != null) {
         for(TestRunLifecycleStatus status : statuses) {
             if(status.toString().equals(structure.getStatus())){
                 return true;
             }
         }
     }
      
      return false;
      
      
   }
   

   public TestRunLifecycleStatus[] getStatuses() {
       return this.statuses;
   }
   

}