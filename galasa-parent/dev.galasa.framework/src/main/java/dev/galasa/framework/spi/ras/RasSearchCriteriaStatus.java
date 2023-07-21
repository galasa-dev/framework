package dev.galasa.framework.spi.ras;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.teststructure.TestStructure;

public class RasSearchCriteriaStatus implements IRasSearchCriteria {
   
   private final String[] statuses;
   
   public RasSearchCriteriaStatus(@NotNull String... statuses) {
      this.statuses = statuses;
   }
   
   @Override
   public boolean criteriaMatched(@NotNull TestStructure structure) {
      
      if(structure == null) {
         return Boolean.FALSE;
      }
      
      if(statuses != null) {
         for(String status : statuses) {
             if(status.equals(structure.getStatus())){
                 return Boolean.TRUE;
             }
         }
     }
      
      return Boolean.FALSE;
      
      
   }
   

   public String[] getStatuses() {
       return this.statuses;
   }
   

}