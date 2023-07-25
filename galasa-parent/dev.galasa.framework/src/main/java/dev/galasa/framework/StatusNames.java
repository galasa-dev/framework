/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework;

import java.util.*;

public class StatusNames {
    public enum statuses{
        finished,
        building,
        generating,
        running,
        rundone,
        up,
        started,
        provstart,
        ending
        ;

        /** 
         * @return A list of possible status names, as strings
         */
		public static List<String> getAll() {
            List<String> validStatuses = new ArrayList<String>();
            for (statuses status : statuses.values()){
                validStatuses.add(status.toString());
            }
            return validStatuses;
        }
    }
}
