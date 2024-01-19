 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

public enum RunActionType {
    RESET("reset"),
    DELETE("delete")
    ;
    private String value;

    private RunActionType(String type){
        this.value = type;
    }

    public static RunActionType getfromString(String typeAsString){
        RunActionType match = null;
        for (RunActionType type : RunActionType.values()){
            if (type.toString().equalsIgnoreCase(typeAsString)){
                match = type;
            }
        }
        return match;
    }

    @Override
    public String toString(){
        return value;
    }

}