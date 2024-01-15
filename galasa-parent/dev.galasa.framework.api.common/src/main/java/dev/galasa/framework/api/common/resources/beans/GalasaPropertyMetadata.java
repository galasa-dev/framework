package dev.galasa.framework.api.common.resources.beans;

public class GalasaPropertyMetadata {
    private String namespace;
    private String name;

    public GalasaPropertyMetadata (String namespace, String name){
        this.namespace = namespace;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }
}

