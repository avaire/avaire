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

package com.avairebot.handlers.adapter;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.ReactionController;
import com.avairebot.database.query.QueryBuilder;
import com.avairebot.database.transformers.ReactionTransformer;
import com.avairebot.scheduler.tasks.DrainReactionRoleQueueTask;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;

public class ReactionEmoteEventAdapter extends EventAdapter {

    public ReactionEmoteEventAdapter(AvaIre avaire) {
        super(avaire);
    }

    public void onEmoteRemoved(EmoteRemovedEvent event) {
        Collection collection = ReactionController.fetchReactions(avaire, event.getGuild());
        if (collection == null || collection.isEmpty()) {
            return;
        }

        boolean wasActionTaken = false;
        for (DataRow row : collection) {
            ReactionTransformer transformer = new ReactionTransformer(row);

            if (transformer.removeReaction(event.getEmote())) {
                try {
                    QueryBuilder query = avaire.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
                        .useAsync(true)
                        .where("guild_id", transformer.getGuildId())
                        .where("message_id", transformer.getMessageId());

                    if (transformer.getRoles().isEmpty()) {
                        query.delete();
                    } else {
                        query.update(statement -> {
                            statement.set("roles", AvaIre.gson.toJson(transformer.getRoles()));
                        });
                    }

                    wasActionTaken = true;
                } catch (SQLException ignored) {
                    // Since the query is running asynchronously the error will never
                    // actually be catched here since the database thread running
                    // the query will log the error instead.
                }
            }
        }

        if (wasActionTaken) {
            ReactionController.forgetCache(event.getGuild().getIdLong());
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        ReactionTransformer transformer = getReactionTransformerFromMessageIdAndCheckPermissions(
            event.getGuild(), event.getMessageId(), event.getReactionEmote().getEmote().getIdLong()
        );

        if (transformer == null) {
            return;
        }

        Role role = event.getGuild().getRoleById(transformer.getRoleIdFromEmote(event.getReactionEmote().getEmote()));
        if (role == null) {
            return;
        }

        if (RoleUtil.hasRole(event.getMember(), role) || !event.getGuild().getSelfMember().canInteract(role)) {
            return;
        }

        DrainReactionRoleQueueTask.queueReactionActionEntity(new DrainReactionRoleQueueTask.ReactionActionEntity(
            event.getGuild().getIdLong(),
            event.getMember().getUser().getIdLong(),
            role.getIdLong(),
            DrainReactionRoleQueueTask.ReactionActionType.ADD
        ));
    }

    @SuppressWarnings("ConstantConditions")
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        ReactionTransformer transformer = getReactionTransformerFromMessageIdAndCheckPermissions(
            event.getGuild(), event.getMessageId(), event.getReactionEmote().getEmote().getIdLong()
        );

        if (transformer == null) {
            return;
        }

        Role role = event.getGuild().getRoleById(transformer.getRoleIdFromEmote(event.getReactionEmote().getEmote()));
        if (role == null) {
            return;
        }

        if (!RoleUtil.hasRole(event.getMember(), role) || !event.getGuild().getSelfMember().canInteract(role)) {
            return;
        }

        DrainReactionRoleQueueTask.queueReactionActionEntity(new DrainReactionRoleQueueTask.ReactionActionEntity(
            event.getGuild().getIdLong(),
            event.getMember().getUser().getIdLong(),
            role.getIdLong(),
            DrainReactionRoleQueueTask.ReactionActionType.REMOVE
        ));
    }

    private ReactionTransformer getReactionTransformerFromMessageIdAndCheckPermissions(@Nonnull Guild guild, @Nonnull String messageId, long emoteId) {
        if (!hasPermission(guild)) {
            return null;
        }

        Collection collection = ReactionController.fetchReactions(avaire, guild);
        if (collection == null || collection.isEmpty()) {
            return null;
        }

        ReactionTransformer transformer = getReactionTransformerFromId(collection, messageId);
        if (transformer == null || !transformer.getRoles().containsKey(emoteId)) {
            return null;
        }
        return transformer;
    }

    private boolean hasPermission(Guild guild) {
        return guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)
            || guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES);
    }

    @Nullable
    private ReactionTransformer getReactionTransformerFromId(@Nonnull Collection collection, @Nonnull String messageId) {
        List<DataRow> messages = collection.where("message_id", messageId);
        if (messages.isEmpty()) {
            return null;
        }
        return new ReactionTransformer(messages.get(0));
    }
}
