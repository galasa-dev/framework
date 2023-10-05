/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaStatus implements IRasSearchCriteria {
   
   private final List<TestRunLifecycleStatus> statuses;
   
   public RasSearchCriteriaStatus(@NotNull List<TestRunLifecycleStatus> statuses) {
      this.statuses = statuses;
   }
   
   @Override
   public boolean criteriaMatched(@NotNull TestStructure structure) {
      
      for(TestRunLifecycleStatus status : statuses) {
            if(status.toString().equals(structure.getStatus())){
               return true;
            }
      }
      return false;
   }
   

   public List<TestRunLifecycleStatus> getStatuses() {
      return this.statuses;
   }

   public String[] getStatusesAStrings() {
      List<String> statuseStrings = new ArrayList<String>();
      for (TestRunLifecycleStatus status : this.statuses){
         statuseStrings.add(status.toString());
      }
      return statuseStrings.toArray(new String[0]);
   }
}