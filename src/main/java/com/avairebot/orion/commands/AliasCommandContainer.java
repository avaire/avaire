package com.avairebot.orion.commands;

public class AliasCommandContainer extends CommandContainer {

    private final String[] aliasArguments;

    public AliasCommandContainer(CommandContainer container, String[] aliasArguments) {
        super(container.getCommand(), container.getCategory());

        this.aliasArguments = aliasArguments;
    }

    public String[] getAliasArguments() {
        return aliasArguments;
    }
}
