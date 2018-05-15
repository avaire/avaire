package com.avairebot.plugin.translators;

import com.avairebot.contracts.plugin.Translator;
import com.avairebot.plugin.PluginHolder;
import com.avairebot.plugin.PluginLoader;
import com.avairebot.plugin.PluginRepository;

import java.util.List;

public class PluginLoaderTranslator implements Translator {

    private final PluginLoader loader;
    private PluginHolder holder = null;

    public PluginLoaderTranslator(PluginLoader loader, List<PluginHolder> plugins) {
        this.loader = loader;

        for (PluginHolder plugin : plugins) {
            if (plugin.getName().equalsIgnoreCase(loader.getName())) {
                this.holder = plugin;
                break;
            }
        }
    }

    @Override
    public String getName() {
        return loader.getName();
    }

    @Override
    public String getDescription() {
        if (loader.getDescription() != null) {
            return loader.getDescription();
        }
        return holder == null ? null : holder.getDescription();
    }

    @Override
    public List<String> getAuthors() {
        if (loader.getAuthors().isEmpty() && holder != null) {
            return holder.getAuthors();
        }
        return loader.getAuthors();
    }

    @Override
    public PluginRepository getRepository() {
        if (holder == null) {
            return null;
        }
        return holder.getRepository();
    }

    @Override
    public boolean isInstalled() {
        return true;
    }
}
