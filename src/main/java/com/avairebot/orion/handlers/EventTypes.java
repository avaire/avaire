package com.avairebot.orion.handlers;

public enum EventTypes {
    GuildMemberJoinEvent(GuildMemberJoin.class),
    GuildRoleDeleteEvent(GuildRoleDelete.class),
    GuildRoleUpdateEvent(GuildRoleUpdate.class),
    MessageReceivedEvent(MessageCreate.class);

    private final Class instance;

    EventTypes(Class instance) {
        this.instance = instance;
    }

    public <T> Class<T> getInstance() {
        return instance;
    }
}
