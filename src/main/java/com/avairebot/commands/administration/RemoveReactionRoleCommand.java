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
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.ReactionController;
import com.avairebot.database.transformers.GuildTypeTransformer;
import com.avairebot.database.transformers.ReactionTransformer;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Emote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemoveReactionRoleCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(RemoveReactionRoleCommand.class);

    public RemoveReactionRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Remove Reaction Role Command";
    }

    @Override
    public String getDescription() {
        return "--- TODO ---";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "--- TODO ---"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "--- TODO ---"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("rrr");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
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
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        Collection collection = ReactionController.fetchReactions(avaire, context.getGuild());
        if (collection == null) {
            return sendErrorMessage(context, "Failed to load the reaction roles from the server, please try again later, if this continues to happen please report it to one of my developers on the official AvaIre Discord Server.");
        }

        if (collection.isEmpty()) {
            return sendErrorMessage(context, "The server doesn't have any reaction roles right now, you can create one using the\n{0}arr <emote> <role>",
                CommandHandler.getCommand(AddReactionRoleCommand.class)
                    .getCategory().getPrefix(context.getMessage())
            );
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "message id");
        }

        int index = NumberUtil.parseInt(args[0], -1);
        if (index < 1) {
            return sendErrorMessage(context, "Invalid `message id` given, the `message id` must be at least 1 or greater.");
        }

        if (index > collection.size()) {
            return sendErrorMessage(context, "Invalid `message id` given, the `message id` cannot be higher than the amount of reaction roles the server currently has, which is `{0}` right now.",
                String.valueOf(collection.size())
            );
        }

        DataRow row = collection.get(index - 1);
        if (row == null) {
            return sendErrorMessage(context, "Something went wrong while trying to fetch the reaction role message with an ID of `{0}`, if this issue continues to happen, please report it to one of my developers.",
                String.valueOf(index)
            );
        }

        if (args.length > 1) {
            return removeSingleRoleOrEmoteFromMessage(context, Arrays.copyOfRange(args, 1, args.length), row);
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .where("message_id", row.getString("message_id"))
                .delete();

            ReactionController.forgetCache(context.getGuild().getIdLong());

            context.makeSuccess("The reaction role message with an ID of **#:id** has successfully been deleted!")
                .set("id", index)
                .queue();
        } catch (SQLException e) {
            log.error("Failed to delete the reaction role message from the database, error: {}",
                e.getMessage(), e
            );

            return sendErrorMessage(context, "Failed to delete the reaction role message from the database, please try again later.");
        }

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean removeSingleRoleOrEmoteFromMessage(CommandMessage context, String[] args, DataRow row) {
        if (context.getMessage().getEmotes().isEmpty()) {
            return sendErrorMessage(context, "You must include the emote you want to remove from the reaction message.");
        }

        Emote emote = context.getMessage().getEmotes().get(0);
        if (emote.getGuild() == null || emote.getGuild().getIdLong() != context.getGuild().getIdLong()) {
            return sendErrorMessage(context, "The emote does not belong to this server, you can only use emotes from this server as reaction emotes.");
        }
        ReactionTransformer transformer = new ReactionTransformer(row);
        if (!transformer.removeReaction(emote)) {
            return sendErrorMessage(context, "The specified reaction message doesn't have a reaction role attached to it using the {0} emote.",
                emote.getAsMention()
            );
        }
        try {
            if (transformer.getRoles().isEmpty()) {
                avaire.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
                    .where("guild_id", context.getGuild().getId())
                    .where("message_id", row.getString("message_id"))
                    .delete();
            } else {
                avaire.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
                    .where("guild_id", transformer.getGuildId())
                    .where("message_id", transformer.getMessageId())
                    .update(statement -> {
                        statement.set("roles", AvaIre.gson.toJson(transformer.getRoles()));
                    });
            }

            ReactionController.forgetCache(context.getGuild().getIdLong());
            GuildTypeTransformer.GuildTypeLimits.GuildReactionRoles reactionLimits = context.getGuildTransformer()
                .getType().getLimits().getReactionRoles();

            context.makeSuccess("The :emote emote has been successfully removed from the reaction message.\nThe message has `:roleSlots` more reaction-role slots available for the message, and `:messageSlots` reaction-message slots available.")
                .set("emote", emote.getAsMention())
                .set("roleSlots", reactionLimits.getRolesPerMessage() - transformer.getRoles().size())
                .set("messageSlots", reactionLimits.getMessages() - ReactionController.fetchReactions(avaire, context.getGuild()).size())
                .queue(successMessage -> successMessage.delete().queueAfter(15, TimeUnit.SECONDS));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
