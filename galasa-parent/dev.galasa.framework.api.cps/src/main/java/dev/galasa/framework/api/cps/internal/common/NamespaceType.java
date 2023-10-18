 /*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;
 
 public enum NamespaceType{
        NORMAL("normal"),
        SECURE("secure")
        ;
        private String value;

        private NamespaceType(String type){
            this.value = type;
        }

        protected static NamespaceType getfromString(String typeAsString){
            NamespaceType match = null;
            for (NamespaceType type : NamespaceType.values()){
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