package com.avairebot.blacklist;

import com.avairebot.utilities.NumberUtil;

import javax.annotation.Nonnull;

public enum Scope {

    USER(0, 'U'),
    GUILD(1, 'G');

    private final int id;
    private final char prefix;

    Scope(int id, char prefix) {
        this.id = id;
        this.prefix = prefix;
    }

    public static Scope fromId(int id) {
        for (Scope scope : values()) {
            if (scope.getId() == id) {
                return scope;
            }
        }
        return null;
    }

    public static Scope parse(@Nonnull String string) {
        int parsedInt = NumberUtil.parseInt(string, -1);
        for (Scope scope : values()) {
            if (string.toUpperCase().charAt(0) == scope.getPrefix()) {
                return scope;
            }

            if (parsedInt == scope.getId()) {
                return scope;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public char getPrefix() {
        return prefix;
    }
}
