package com.avairebot;

public enum Environment {

    PRODUCTION("production", "prod"),
    DEVELOPMENT("development", "dev");

    private final String name;
    private final String alias;

    Environment(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public static Environment fromName(String name) {
        for (Environment environment : values()) {
            if (environment.getName().equalsIgnoreCase(name) || environment.getAlias().equalsIgnoreCase(name)) {
                return environment;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }
}
