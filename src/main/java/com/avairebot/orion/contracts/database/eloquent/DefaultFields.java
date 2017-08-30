package com.avairebot.orion.contracts.database.eloquent;

public abstract class DefaultFields {

    public abstract Modle instance();

    protected String table() {
        return getClass().getSimpleName() + "s";
    }

    protected String primaryKey() {
        return "id";
    }

    protected boolean incrementing() {
        return true;
    }

    protected boolean timestamps() {
        return true;
    }
}
