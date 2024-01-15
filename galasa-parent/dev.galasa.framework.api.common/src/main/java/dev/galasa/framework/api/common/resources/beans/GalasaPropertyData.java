package dev.galasa.framework.api.common.resources.beans;

public class GalasaPropertyData {
    private String value;

    public GalasaPropertyData (String propertyValue){
        this.value = propertyValue;
    }

    public String getValue() {
        return this.value;
    }
}