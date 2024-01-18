package dev.galasa.framework.api.ras.internal.common;

public class RunActionJson {

    private String action;
    private String runName;

    public RunActionJson(String action, String runName){
        this.action = action;
        this.runName = runName;
    }
    
    public String getAction(){
        return this.action;
    }

    public String getRunName(){
        return this.runName;
    }
}
