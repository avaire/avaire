package com.avairebot.orion.database.eloquent;

public abstract class EloquentDefaultFields {

    public abstract Eloquent instance();

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
