package com.avairebot.blacklist;

public class BlacklistEntity {

    private final Scope scope;
    private final long id;

    public BlacklistEntity(Scope scope, long id) {
        this.scope = scope;
        this.id = id;
    }

    public Scope getScope() {
        return scope;
    }

    public long getId() {
        return id;
    }
}
