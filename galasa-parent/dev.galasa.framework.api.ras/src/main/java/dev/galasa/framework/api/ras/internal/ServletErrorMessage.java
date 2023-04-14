/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

public enum ServletErrorMessage {

    GAL5001_INVALID_DATE_TIME_FIELD                 (5001,"E: Error parsing the date-time field ''{0}''. Invalid value ''{1}''. Expecting a java LocalDateTime format. For example: ''2023-04-11T09:42:06.589180Z''"),
    GAL5002_INVALID_RUN_ID                          (5002,"E: Error retrieving ras run from RunID ''{0}''"),
    GAL5003_ERROR_RETRIEVEING_RUNS                  (5003,"E: Error retrieving runs"),
    GAL5004_ERROR_RETRIEVING_PAGE                   (5004,"E: Error retrieving page"),
    ;

    private String template ;
    private int templateNumber;

    private ServletErrorMessage(int templateNumber , String template) {
        this.template = "GAL"+Integer.toString(templateNumber)+template ;
        this.templateNumber = templateNumber ;
    }

    public String toString() {
        return this.template ;
    }

    public int getTemplateNumber() {
        return this.templateNumber;
    }
}