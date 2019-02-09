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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.controllers.ReactionController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.GuildTypeTransformer;
import com.avairebot.database.transformers.ReactionTransformer;
import com.avairebot.utilities.RestActionUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AddReactionRoleCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(AddReactionRoleCommand.class);

    public AddReactionRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Add Reaction Role Command";
    }

    @Override
    public String getDescription() {
        return "Adds a reaction emote to the last message sent in the channel, and attaches a role to the emote, users can then reaction to the message using the emote to get the role linked with the emote.\n**Note:** reaction emotes has to belong to the server the message is created on, you can't use global emotes or emotes from other servers.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <emote> <role name>` - Creates a reaction role with the given emote and role name."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command :avaPotato: Potato` - Gives users the Potato role when the reaction with the :avaPotato: reaction."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            ListReactionRoleCommand.class,
            RemoveReactionRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("arr");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "require:bot,general.manage_roles",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(
            CommandGroups.ROLE_ASSIGNMENTS
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading",
                "guild settings"
            );
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "reaction emote");
        }

        Emote emote = getEmote(context, args);
        if (emote == null || emote.getGuild() == null || emote.getGuild().getIdLong() != context.getGuild().getIdLong()) {
            return sendErrorMessage(context, context.i18n("emoteDoestBelongToServer"));
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "reaction role");
        }

        Role role = RoleUtil.getRoleFromMentionsOrName(
            context.getMessage(),
            String.join(" ", Arrays.copyOfRange(
                args, 1, args.length
            ))
        );

        if (role == null) {
            return sendErrorMessage(context, "errors.invalidProperty", "reaction role", "role");
        }

        if (!RoleUtil.canInteractWithRole(context.getMessage(), role)) {
            return false;
        }

        context.getChannel().getHistory().retrievePast(2).queue(messages -> {
            if (messages.size() != 2) {
                return;
            }

            context.delete().queue(null, RestActionUtil.ignore);

            Message message = messages.get(1);
            ReactionTransformer reactionTransformer = ReactionController.fetchReactionFromMessage(avaire, message);
            if (reactionTransformer == null) {
                createNewReactionRoleMessage(context, guildTransformer, emote, role, message);
                return;
            }

            if (reactionTransformer.getRoles().size() >= guildTransformer.getType().getLimits().getReactionRoles().getRolesPerMessage()) {
                context.makeWarning(context.i18n("messageHasNoSlots"))
                    .queue(noSlotsMessage -> noSlotsMessage.delete().queueAfter(15, TimeUnit.SECONDS, null, RestActionUtil.ignore));
                return;
            }

            reactionTransformer.addReaction(emote, role);

            try {
                avaire.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
                    .where("guild_id", reactionTransformer.getGuildId())
                    .where("channel_id", reactionTransformer.getChannelId())
                    .where("message_id", reactionTransformer.getMessageId())
                    .update(statement -> {
                        statement.set("roles", AvaIre.gson.toJson(reactionTransformer.getRoles()));
                    });

                GuildTypeTransformer.GuildTypeLimits.GuildReactionRoles reactionLimits = guildTransformer.getType().getLimits().getReactionRoles();

                message.addReaction(emote).queue();
                context.makeSuccess(context.i18n("success"))
                    .set("role", role.getAsMention())
                    .set("emote", emote.getAsMention())
                    .set("roleSlots", reactionLimits.getRolesPerMessage() - reactionTransformer.getRoles().size())
                    .set("messageSlots", reactionLimits.getMessages() - ReactionController.fetchReactions(avaire, context.getGuild()).size())
                    .queue(successMessage -> successMessage.delete().queueAfter(15, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                ReactionController.forgetCache(context.getGuild().getIdLong());
            } catch (SQLException e) {
                log.error("Failed to save the reaction role to the database: {}", e.getMessage(), e);
                sendErrorMessage(context, "Failed to save the reaction role to the database, {0}", e.getMessage());
            }
        });

        return true;
    }

    @Nullable
    private Emote getEmote(CommandMessage context, String[] args) {
        if (!context.getMessage().getEmotes().isEmpty()) {
            return context.getMessage().getEmotes().get(0);
        }

        try {
            long id = Long.parseLong(args[0].trim());

            return context.getGuild().getEmoteById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean createNewReactionRoleMessage(CommandMessage context, GuildTransformer transformer, Emote emote, Role role, Message message) {
        Collection collection = ReactionController.fetchReactions(avaire, context.getGuild());
        if (collection == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading",
                "reaction roles"
            );
        }

        if (collection.size() >= transformer.getType().getLimits().getReactionRoles().getMessages()) {
            context.makeWarning(context.i18n("serverHasNoSlots"))
                .queue(noSlotsMessage -> noSlotsMessage.delete().queueAfter(15, TimeUnit.SECONDS, null, RestActionUtil.ignore));
            return false;
        }

        message.addReaction(emote).queue(aVoid -> {
            ReactionTransformer reactionTransformer = new ReactionTransformer(null);
            reactionTransformer.addReaction(emote, role);

            try {
                String messageContent = message.getContentStripped();
                if (messageContent.isEmpty() && !message.getEmbeds().isEmpty()) {
                    messageContent = message.getEmbeds().get(0).getDescription();
                }

                String finalMessageContent = messageContent;
                avaire.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
                    .insert(statement -> {
                        statement.set("guild_id", message.getGuild().getId());
                        statement.set("channel_id", message.getChannel().getId());
                        statement.set("message_id", message.getId());
                        statement.set("roles", AvaIre.gson.toJson(reactionTransformer.getRoles()));
                        statement.set("snippet", finalMessageContent.substring(
                            0, Math.min(finalMessageContent.length(), 64)
                        ), true);
                    });

                GuildTypeTransformer.GuildTypeLimits.GuildReactionRoles reactionLimits = transformer.getType().getLimits().getReactionRoles();

                context.makeSuccess(context.i18n("success"))
                    .set("role", role.getAsMention())
                    .set("emote", emote.getAsMention())
                    .set("roleSlots", reactionLimits.getRolesPerMessage() - reactionTransformer.getRoles().size())
                    .set("messageSlots", reactionLimits.getMessages() - (collection.size() + 1))
                    .queue(successMessage -> successMessage.delete().queueAfter(15, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                ReactionController.forgetCache(context.getGuild().getIdLong());
            } catch (SQLException e) {
                log.error("Failed to save the reaction role to the database: {}", e.getMessage(), e);
                sendErrorMessage(context, "Failed to save the reaction role to the database, {0}", e.getMessage());
            }
        });

        return true;
    }
}
