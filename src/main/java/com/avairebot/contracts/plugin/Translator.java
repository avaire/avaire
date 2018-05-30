package com.avairebot.contracts.plugin;

import com.avairebot.plugin.PluginRepository;

import java.util.List;

public interface Translator {

    /**
     * Gets the name of the plugin.
     *
     * @return The name of the plugin.
     */
    String getName();

    /**
     * Gets the description of the plugin.
     *
     * @return The description of the plugin.
     */
    String getDescription();

    /**
     * Gets the list of authors who created the plugin.
     *
     * @return The list of authros who created the plugin.
     */
    List<String> getAuthors();

    /**
     * Gets the plugin repository object for communicating with the plugins repository.
     *
     * @return The plugin repository object.
     */
    PluginRepository getRepository();

    /**
     * Checks if the plugin has been installed or not.
     *
     * @return <code>True</code> if the plugin has been installed, <code>False</code> otherwise.
     */
    boolean isInstalled();
}
