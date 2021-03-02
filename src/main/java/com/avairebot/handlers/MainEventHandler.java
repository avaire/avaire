/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.handlers.adapter.*;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateRegionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainEventHandler extends EventHandler {

    private final RoleEventAdapter roleEvent;
    private final MemberEventAdapter memberEvent;
    private final ChannelEventAdapter channelEvent;
    private final MessageEventAdapter messageEvent;
    private final GuildStateEventAdapter guildStateEvent;
    private final JDAStateEventAdapter jdaStateEventAdapter;
    private final ChangelogEventAdapter changelogEventAdapter;
    private final ReactionEmoteEventAdapter reactionEmoteEventAdapter;

    public static final Cache<Long, Boolean> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(MainEventHandler.class);

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public MainEventHandler(AvaIre avaire) {
        super(avaire);

        this.roleEvent = new RoleEventAdapter(avaire);
        this.memberEvent = new MemberEventAdapter(avaire);
        this.channelEvent = new ChannelEventAdapter(avaire);
        this.messageEvent = new MessageEventAdapter(avaire);
        this.guildStateEvent = new GuildStateEventAdapter(avaire);
        this.jdaStateEventAdapter = new JDAStateEventAdapter(avaire);
        this.changelogEventAdapter = new ChangelogEventAdapter(avaire);
        this.reactionEmoteEventAdapter = new ReactionEmoteEventAdapter(avaire);
    }

    @Override
    public void onGenericEvent(GenericEvent event) {
        prepareGuildMembers(event);

        Metrics.jdaEvents.labels(event.getClass().getSimpleName()).inc();
    }

    @Override
    public void onReady(ReadyEvent event) {
        jdaStateEventAdapter.onConnectToShard(event.getJDA());
    }

    @Override
    public void onResume(ResumedEvent event) {
        jdaStateEventAdapter.onConnectToShard(event.getJDA());
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        jdaStateEventAdapter.onConnectToShard(event.getJDA());
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
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        channelEvent.onVoiceChannelDelete(event);
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        channelEvent.updateChannelData(event.getGuild());
        channelEvent.onTextChannelDelete(event);
    }

    @Override
    public void onTextChannelCreate(TextChannelCreateEvent event) {
        channelEvent.updateChannelData(event.getGuild());
    }

    @Override
    public void onTextChannelUpdateName(TextChannelUpdateNameEvent event) {
        channelEvent.updateChannelData(event.getGuild());
    }

    @Override
    public void onTextChannelUpdatePosition(TextChannelUpdatePositionEvent event) {
        channelEvent.updateChannelData(event.getGuild());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!avaire.getSettings().isMusicOnlyMode()) {
            memberEvent.onGuildMemberJoin(event);
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        if (!avaire.getSettings().isMusicOnlyMode()) {
            memberEvent.onGuildMemberRemove(event);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (changelogEventAdapter.isChangelogMessage(event.getChannel())) {
            changelogEventAdapter.onMessageReceived(event);
        }

        messageEvent.onMessageReceived(event);
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        if (changelogEventAdapter.isChangelogMessage(event.getChannel())) {
            changelogEventAdapter.onMessageDelete(event);
        }

        messageEvent.onMessageDelete(event.getChannel(), Collections.singletonList(event.getMessageId()));
    }

    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        messageEvent.onMessageDelete(event.getChannel(), event.getMessageIds());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (changelogEventAdapter.isChangelogMessage(event.getChannel())) {
            changelogEventAdapter.onMessageUpdate(event);
        }

        messageEvent.onMessageUpdate(event);
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        roleEvent.updateRoleData(event.getGuild());
        roleEvent.onRoleUpdateName(event);
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        roleEvent.updateRoleData(event.getGuild());
        roleEvent.onRoleDelete(event);
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        roleEvent.updateRoleData(event.getGuild());
    }

    @Override
    public void onRoleUpdatePosition(RoleUpdatePositionEvent event) {
        roleEvent.updateRoleData(event.getGuild());
    }

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {
        roleEvent.updateRoleData(event.getGuild());
    }

    @Override
    public void onUserUpdateDiscriminator(UserUpdateDiscriminatorEvent event) {
        if (!avaire.getSettings().isMusicOnlyMode()) {
            PlayerController.updateUserData(event.getUser());
        }
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        if (!avaire.getSettings().isMusicOnlyMode()) {
            PlayerController.updateUserData(event.getUser());
        }
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        if (!avaire.getSettings().isMusicOnlyMode()) {
            PlayerController.updateUserData(event.getUser());
        }
    }

    @Override
    public void onEmoteRemoved(EmoteRemovedEvent event) {
        reactionEmoteEventAdapter.onEmoteRemoved(event);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (isValidMessageReactionEvent(event)) {
            reactionEmoteEventAdapter.onMessageReactionAdd(event);
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (isValidMessageReactionEvent(event)) {
            reactionEmoteEventAdapter.onMessageReactionRemove(event);
        }
    }

    private boolean isValidMessageReactionEvent(GenericMessageReactionEvent event) {
        return event.isFromGuild() && event.getReactionEmote().isEmote();
    }

    private void prepareGuildMembers(GenericEvent event) {
        if (event instanceof GenericMessageEvent) {
            GenericMessageEvent genericMessageEvent = (GenericMessageEvent) event;

            if (genericMessageEvent.isFromGuild()) {
                loadGuildMembers(genericMessageEvent.getGuild());
            }
        } else if (event instanceof GenericRoleEvent) {
            GenericRoleEvent genericRoleEvent = (GenericRoleEvent) event;

            loadGuildMembers(genericRoleEvent.getGuild());
        }
    }

    private void loadGuildMembers(Guild guild) {
        if (guild.isLoaded()) {
            return;
        }

        CacheUtil.getUncheckedUnwrapped(cache, guild.getIdLong(), () -> {
            log.debug("Lazy-loading members for guild: {} (ID: {})", guild.getName(), guild.getIdLong());
            Task<List<Member>> task = guild.loadMembers();

            guild.getMemberCount();

            task.onSuccess(members -> {
                log.debug("Lazy-loading for guild {} is done, loaded {} members",
                    guild.getId(), members.size()
                );

                cache.invalidate(guild.getIdLong());
            });

            task.onError(throwable -> log.error("Failed to lazy-load guild members for {}, error: {}",
                guild.getIdLong(), throwable.getMessage(), throwable
            ));

            return true;
        });
    }
}
