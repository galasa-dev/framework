package io.ejat;

import javax.validation.constraints.NotNull;

/**
 * Provides access to the Confidential Text Services which will replace registered text
 * with a safe string.  Can be used for tokens, passwords etc to prevent it from being 
 * exposed in logs and screen displays.
 * 
 * @author Michael Baylis
 *
 */
public interface IConfidentialTextService {

	/**
	 * Register a string to the Confidential Text Service.  Will also write a message to the 
	 * run log showing what the text the string will be replaced with.
	 * 
	 * @param confidentialText - The text to be filtered.
	 * @param description  - A description to log.
	 * @return The string that will be used to replace the confidential text
	 */
	String registerConfidentialText(@NotNull String confidentialText, @NotNull String description);
	
	/**
	 * Scan the text for registered strings and replace them with safe strings.
	 * 
	 * @param text - Text to be filtered
	 * @return - The text with confidential text replaced by safe strings
	 */
	@NotNull 
	String filterText(@NotNull String text);
}
