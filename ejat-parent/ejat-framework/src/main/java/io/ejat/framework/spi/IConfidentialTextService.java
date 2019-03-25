package io.ejat.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * The confidential text services provides a manager with the ability to registered passwords, usernames, keys 
 * and other confidnetial texts so that they can be obscured inside logs and outputs.
 * 
 * @author James Davies
 */
public interface IConfidentialTextService {

    /**
     * Registers the service with the framework, ensuring only one service is operational at one time.
     * 
     * @param frameworkInitialisation
     * @throws ConfidentialTextException
     */
    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ConfidentialTextException;

    /**
     * Regsiters a confidential text on the service. When any log or output is passed through this service it 
     * will then obscure it with a numbered Tag.
     * 
     * @param confidentialString - the string to be registered.
     * @param comment - a comment explaining the string.
     */
    void registerText(String confidentialString, String comment);

    /**
     * Removed any number of registered texts from a given log or output.
     * 
     * @param text - the orginal log ior output.
     * @return -the obscured log or output.
     */
    String removeConfidentialText(String text);

} 