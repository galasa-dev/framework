/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

public enum ServletErrorMessage {

    GAL5001_INVALID_DATE_TIME_FIELD(5001,"E: Error parsing the date-time field {0}"),
    GAL5002_INVALID_DATE_TIME_FIELD(5002,"E: Error parsing the date-time field {0}"),
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