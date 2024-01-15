/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import dev.galasa.framework.spi.FrameworkErrorCode;
import dev.galasa.framework.spi.FrameworkException;

/**
 * A class which can validate whether a resources use valid characters or not.
 */
public class ResourceNameValidator {

    public static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String digits = "0123456789";



    public static final String namespaceValidFirstCharacters = letters ;
    public static final String namespaceValidFollowingCharacters = namespaceValidFirstCharacters + digits ;

    public static final String propertyValidFirstCharacters = letters ;
    public static final String propertySeparators = ".-_";
    public static final String propertyValidFollowingCharacters = propertyValidFirstCharacters
            + digits + propertySeparators ;

    public void assertNamespaceCharPatternIsValid(String possibleNamespaceName) throws FrameworkException {


        // Guard against null or empty namespace name.
        if (possibleNamespaceName == null || possibleNamespaceName.isEmpty() ) {
            throw new FrameworkException(FrameworkErrorCode.INVALID_NAMESPACE,
                    "Invalid namespace. Namespace is empty.");
        }

        for(int charIndex = 0 ; charIndex < possibleNamespaceName.length() ; charIndex +=1 ) {
            int c = possibleNamespaceName.charAt(charIndex);
            if (charIndex==0) {
                // Check the first character.
                if (namespaceValidFirstCharacters.indexOf(c) < 0) {
                    String messageTemplate = "Invalid namespace name. '%s' must not start with the '%s' character." +
                            " Allowable first characters are 'a'-'z' or 'A'-'Z'.";
                    String message = String.format(messageTemplate, possibleNamespaceName, Character.toString(c));
                    throw new FrameworkException(FrameworkErrorCode.INVALID_NAMESPACE, message);
                }
            } else {
                // Check a following character.
                if(namespaceValidFollowingCharacters.indexOf(c) < 0 ) {
                    String messageTemplate = "Invalid namespace name. '%s' must not contain the '%s' character." +
                            " Allowable characters after the first character are 'a'-'z', 'A'-'Z', '0'-'9'.";
                    String message = String.format(messageTemplate, possibleNamespaceName, Character.toString(c));
                    throw new FrameworkException(FrameworkErrorCode.INVALID_NAMESPACE, message);
                }
            }
        }
    }


    /**
     * Validate the prefix of a cps property.
     * @param prefix The prefix to validate.
     * @throws FrameworkException If the property prefix is invalid.
     */
    public void assertPropertyCharPatternPrefixIsValid(String prefix) throws FrameworkException {

        // Guard against null or empty prefix
        if (prefix == null || prefix.isEmpty() ) {
            throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY,
                    "Invalid property name prefix. Prefix is missing or empty.");
        }

        for(int charIndex = 0 ; charIndex < prefix.length() ; charIndex +=1 ) {
            int c = prefix.charAt(charIndex);
            if (charIndex==0) {
                // Check the first character.
                if (propertyValidFirstCharacters.indexOf(c) < 0) {
                    String messageTemplate = "Invalid property name prefix. '%s' must not start with the '%s' character." +
                            " Allowable first characters are 'a'-'z' or 'A'-'Z'.";
                    String message = String.format(messageTemplate, prefix, Character.toString(c));
                    throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY, message);
                }
            } else {
                // Check a following character.
                if(propertyValidFollowingCharacters.indexOf(c) < 0 ) {
                    String messageTemplate = "Invalid property name prefix. '%s' must not contain the '%s' character." +
                            " Allowable characters after the first character are 'a'-'z', 'A'-'Z', '0'-'9',"+
                            " '-' (dash), '.' (dot) and '_' (underscore).";
                    String message = String.format(messageTemplate, prefix, Character.toString(c));
                    throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY, message);
                }
            }
        }
    }

    public void assertPropertyCharPatternSuffixIsValid(String suffix) throws FrameworkException {
        // Guard against null or empty prefix
        if (suffix == null || suffix.isEmpty() ) {
            throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY,
                    "Invalid property name. Property name is missing or empty.");
        }

        for(int charIndex = 0 ; charIndex < suffix.length() ; charIndex +=1 ) {
            int c = suffix.charAt(charIndex);
            if (charIndex==0) {
                // Check the first character.
                if (propertyValidFirstCharacters.indexOf(c) < 0) {
                    String messageTemplate = "Invalid property name suffix. '%s' must not start with the '%s' character." +
                            " Allowable first characters are 'a'-'z' or 'A'-'Z'.";
                    String message = String.format(messageTemplate, suffix, Character.toString(c));
                    throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY, message);
                }
            } else {
                // Check a following character.
                if(propertyValidFollowingCharacters.indexOf(c) < 0 ) {
                    String messageTemplate = "Invalid property name suffix. '%s' must not contain the '%s' character." +
                            " Allowable characters after the first character are 'a'-'z', 'A'-'Z', '0'-'9',"+
                            " '-' (dash), '.' (dot) and '_' (underscore).";
                    String message = String.format(messageTemplate, suffix, Character.toString(c));
                    throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY, message);
                }
            }
        }
    }

    public void assertPropertyNameCharPatternIsValid(String propertyName) throws FrameworkException {
        // Guard against null or empty prefix
        if (propertyName == null || propertyName.isEmpty() ) {
            throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY,
                    "Invalid property name. Property name is missing or empty.");
        }

        for(int charIndex = 0 ; charIndex < propertyName.length() ; charIndex +=1 ) {
            int c = propertyName.charAt(charIndex);
            if (charIndex==0) {
                // Check the first character.
                if (propertyValidFirstCharacters.indexOf(c) < 0) {
                    String messageTemplate = "Invalid property name. '%s' must not start with the '%s' character." +
                            " Allowable first characters are 'a'-'z' or 'A'-'Z'.";
                    String message = String.format(messageTemplate, propertyName, Character.toString(c));
                    throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY, message);
                }
            } else {
                // Check a following character.
                if(propertyValidFollowingCharacters.indexOf(c) < 0 ) {
                    String messageTemplate = "Invalid property name. '%s' must not contain the '%s' character." +
                            " Allowable characters after the first character are 'a'-'z', 'A'-'Z', '0'-'9',"+
                            " '-' (dash), '.' (dot) and '_' (underscore)";
                    String message = String.format(messageTemplate, propertyName, Character.toString(c));
                    throw new FrameworkException(FrameworkErrorCode.INVALID_PROPERTY, message);
                }
            }
        }
    }
}