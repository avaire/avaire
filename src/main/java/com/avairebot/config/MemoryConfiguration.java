package com.avairebot.config;

import com.avairebot.contracts.config.ConfigurationBase;
import com.avairebot.contracts.config.ConfigurationSection;
import org.apache.commons.lang3.Validate;

import java.util.Map;

/**
 * This is a {@link ConfigurationBase} implementation that does not save or load
 * from any source, and stores all values in memory only.
 * This is useful for temporary Configurations for providing defaults.
 */
public class MemoryConfiguration extends MemorySection implements ConfigurationBase {

    protected MemoryConfigurationOptions options;
    private ConfigurationBase defaults;

    /**
     * Creates an empty {@link MemoryConfiguration} with no default values.
     */
    public MemoryConfiguration() {
    }

    /**
     * Creates an empty {@link MemoryConfiguration} using the specified {@link
     * ConfigurationBase} as a source for all default values.
     *
     * @param defaults Default value provider
     * @throws IllegalArgumentException Thrown if defaults is null
     */
    public MemoryConfiguration(ConfigurationBase defaults) {
        this.defaults = defaults;
    }

    @Override
    public void addDefault(String path, Object value) {
        Validate.notNull(path, "Path may not be null");

        if (defaults == null) {
            defaults = new MemoryConfiguration();
        }

        defaults.set(path, value);
    }

    public void addDefaults(Map<String, Object> defaults) {
        Validate.notNull(defaults, "Defaults may not be null");

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            addDefault(entry.getKey(), entry.getValue());
        }
    }

    public void addDefaults(ConfigurationBase defaults) {
        Validate.notNull(defaults, "Defaults may not be null");

        addDefaults(defaults.getValues(true));
    }

    public ConfigurationBase getDefaults() {
        return defaults;
    }

    public void setDefaults(ConfigurationBase defaults) {
        Validate.notNull(defaults, "Defaults may not be null");

        this.defaults = defaults;
    }

    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    public MemoryConfigurationOptions options() {
        if (options == null) {
            options = new MemoryConfigurationOptions(this);
        }

        return options;
    }
}
