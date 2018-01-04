package com.avairebot.contracts.database.eloquent;

public abstract class DefaultFields {

    public abstract Model instance();

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
