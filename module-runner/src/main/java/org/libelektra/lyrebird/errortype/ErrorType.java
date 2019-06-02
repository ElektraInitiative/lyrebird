package org.libelektra.lyrebird.errortype;

public interface ErrorType {

    /**
     * This command checks if a give path can have an injection of a specific type, e.g. Resource
     * @param path The libelektra path to the setting
     * @return true if applicable, false if not
     */
    boolean isApplicable(String path);

    /**
     * This function executes an injection
     * @param path The libelektra path to the setting
     * @return The message what actually changed in this setting, e.g. "changed true->tre"
     */
    String apply(String path);

    /**
     * Reverts an applied change
     */
    void revert();
}
