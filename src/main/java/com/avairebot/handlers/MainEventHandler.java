package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.handlers.adapter.GuildStateEventAdapter;
import com.avairebot.handlers.adapter.MemberEventAdapter;
import com.avairebot.handlers.adapter.MessageEventAdapter;
import com.avairebot.handlers.adapter.RoleEventAdapter;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.core.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;

public class MainEventHandler extends EventHandler {

    private final RoleEventAdapter roleEvent;
    private final MemberEventAdapter memberEvent;
    private final GuildStateEventAdapter guildStateEvent;
    private final MessageEventAdapter messageEvent;

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public MainEventHandler(AvaIre avaire) {
        super(avaire);

        this.roleEvent = new RoleEventAdapter(avaire);
        this.memberEvent = new MemberEventAdapter(avaire);
        this.messageEvent = new MessageEventAdapter(avaire);
        this.guildStateEvent = new GuildStateEventAdapter(avaire);
    }

    @Override
    public void onGuildUpdateRegion(GuildUpdateRegionEvent event) {
        guildStateEvent.onGuildUpdateRegion(event);
    }

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        guildStateEvent.onGuildUpdateName(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        guildStateEvent.onGuildJoin(event);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        guildStateEvent.onGuildLeave(event);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        memberEvent.onGuildMemberJoin(event);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        memberEvent.onGuildMemberLeave(event);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        messageEvent.onMessageReceived(event);
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        roleEvent.onRoleUpdateName(event);
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        roleEvent.onRoleDelete(event);
    }

    @Override
    public void onUserAvatarUpdate(UserAvatarUpdateEvent event) {
        PlayerController.updateUserData(avaire, event.getUser());
    }

    @Override
    public void onUserNameUpdate(UserNameUpdateEvent event) {
        PlayerController.updateUserData(avaire, event.getUser());
    }
}
