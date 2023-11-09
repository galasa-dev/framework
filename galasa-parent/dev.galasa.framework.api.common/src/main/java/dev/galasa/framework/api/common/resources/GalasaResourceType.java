 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;
 
 public enum GalasaResourceType{
        GALASAPROPERTY("GalasaProperty"),
        ;
        private String value;

        private GalasaResourceType(String type){
            this.value = type;
        }

        public static GalasaResourceType getfromString(String typeAsString){
            GalasaResourceType match = null;
            for (GalasaResourceType type : GalasaResourceType.values()){
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