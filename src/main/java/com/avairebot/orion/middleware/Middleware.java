package com.avairebot.orion.middleware;

public enum Middleware {

    REQUIRE("require", Require.class),
    HAS_ROLE("has-role", HasRole.class),
    THROTTLE("throttle", Throttle.class),
    IS_BOT_ADMIN("isBotAdmin", IsBotAdmin.class),
    PROCESS_COMMAND("process-command", ProcessCommand.class);

    private final String name;
    private final Class instance;

    Middleware(String name, Class instance) {
        this.name = name;
        this.instance = instance;
    }

    public static Middleware fromName(String name) {
        for (Middleware middleware : values()) {
            if (middleware.getName().equalsIgnoreCase(name)) {
                return middleware;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public <T> Class getInstance() {
        return instance;
    }
}
