 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;
 
 public enum GalasaNamespaceType{
        NORMAL("normal"),
        SECURE("secure")
        ;
        private String value;

        private GalasaNamespaceType(String type){
            this.value = type;
        }

        public static GalasaNamespaceType getfromString(String typeAsString){
            GalasaNamespaceType match = null;
            for (GalasaNamespaceType type : GalasaNamespaceType.values()){
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