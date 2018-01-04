package com.avairebot.commands;

public class AliasCommandContainer extends CommandContainer {

    private final String[] aliasArguments;

    public AliasCommandContainer(CommandContainer container, String[] aliasArguments) {
        super(container.getCommand(), container.getCategory(), container.getSourceUri());

        this.aliasArguments = aliasArguments;
    }

    public String[] getAliasArguments() {
        return aliasArguments;
    }
}
