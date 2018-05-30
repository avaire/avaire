package com.avairebot.plugin.translators;

import com.avairebot.contracts.plugin.Translator;
import com.avairebot.plugin.PluginHolder;
import com.avairebot.plugin.PluginRepository;

import java.util.List;

public class PluginHolderTranslator implements Translator {

    private final PluginHolder holder;

    public PluginHolderTranslator(PluginHolder holder) {
        this.holder = holder;
    }

    @Override
    public String getName() {
        return holder.getName();
    }

    @Override
    public String getDescription() {
        return holder.getDescription();
    }

    @Override
    public List<String> getAuthors() {
        return holder.getAuthors();
    }

    @Override
    public PluginRepository getRepository() {
        return holder.getRepository();
    }

    @Override
    public boolean isInstalled() {
        return false;
    }
}
