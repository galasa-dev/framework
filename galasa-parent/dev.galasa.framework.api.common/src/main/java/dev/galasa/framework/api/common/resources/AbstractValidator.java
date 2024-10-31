/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

/**
 * A base validator class that contains commonly-used validation methods
 */
public abstract class AbstractValidator {

    /**
     * Checks whether a given string is in valid Latin-1 format (e.g. characters in the range 0 - 255)
     * 
     * @param str the string to validate
     * @return true if the string is in valid Latin-1 format, or false otherwise
     */
    public boolean isLatin1(String str) {
        boolean isValidLatin1 = true;
        for (char i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 255) {
                isValidLatin1 = false;
                break;
            }
        }
        return isValidLatin1;
    }

    /**
     * Checks whether a given string contains only alphanumeric characters, '-', and '_'
     * 
     * @param str the string to validate
     * @return true if the string contains only alphanumeric characters, '-', and '_', or false otherwise
     */
    public boolean isAlphanumWithDashes(String str) {
        boolean isValid = true;
        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '_') {
                isValid = false;
                break;
            }
        }
        return isValid;
    }
}
