package com.avairebot.handlers;

public enum EventTypes {

    GenericEvent(GenericEvent.class),
    UserUpdateEvent(UserUpdateEvent.class),
    GuildJoinLeaveEvent(GuildJoinLeave.class),
    GuildMemberJoinEvent(GuildMemberJoin.class),
    GuildMemberLeaveEvent(GuildMemberLeave.class),
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
