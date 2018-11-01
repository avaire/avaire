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
import com.avairebot.chat.MessageType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.*;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "welcome-goodbye-message-command")
public class WelcomeMessageCommand extends ChannelModuleCommand {

    public WelcomeMessageCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Welcome Message Command";
    }

    @Override
    public String getDescription() {
        return "Sets the message that should be sent when a user joins the server, this command can only be used if the welcome module is enabled for the current channel.\nThe welcome message has support for [placeholders](https://avairebot.com/docs/placeholders), allowing for customizing the message a bit more for each user.\nhttps://avairebot.com/docs/placeholders";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Resets the welcome message back to the default message.",
            "`:command <message>` - Sets the welcome message to the given message.",
            "`:command embed` - Disables embed messages.",
            "`:command embed <color>` - Enables embed messages with the given color.",
            "`:command <user>` - If a valid username, nickname or user was mentioned, an example message will be sent for the given user."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command Welcome %user%!` - Sets the message to \"Welcome @user\".",
            "`:command embed #ff0000` - Enables embed messages and sets it to red.",
            "`:command @Senither` - Tests the welcome message using the mentioned user."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            GoodbyeCommand.class,
            GoodbyeMessageCommand.class,
            WelcomeCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("welcomemessage", "welmsg");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.JOIN_LEAVE_MESSAGES);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        ChannelTransformer channelTransformer = guildTransformer.getChannel(context.getChannel().getId());

        if (channelTransformer == null || !channelTransformer.getWelcome().isEnabled()) {
            return sendErrorMessage(context, context.i18n("moduleMustBeEnabled", generateCommandPrefix(context.getMessage())));
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("embed")) {
            return handleEmbedOption(context, args, guildTransformer, channelTransformer, () -> {
                String embedColor = getChannelModule(channelTransformer).getEmbedColor();

                context.makeSuccess(context.i18n("message"))
                    .set("status", context.i18n("status." + (embedColor == null ? "disabled" : "enabled")))
                    .set("note", embedColor == null
                        ? context.i18n("note.normal")
                        : context.i18n("note.embed", embedColor))
                    .setColor(embedColor == null ? MessageType.SUCCESS.getColor() : Color.decode(embedColor))
                    .queue();

                return true;
            });
        }

        if (args.length == 1) {
            User user = MentionableUtil.getUser(context, args, 0);

            if (user != null) {
                return sendExampleMessage(context, user, channelTransformer, "Welcome %user% to **%server%!**");
            }
        }

        channelTransformer.getWelcome().setMessage(args.length == 0 ? null : String.join(" ", args));

        return updateDatabase(context, guildTransformer, () -> {
            if (channelTransformer.getWelcome().getMessage() == null) {
                context.makeSuccess(context.i18n("changedToDefault")).queue();
                return true;
            }

            return sendEnableMessage(context, channelTransformer, "Welcome");
        });
    }

    @Override
    public ChannelTransformer.MessageModule getChannelModule(ChannelTransformer transformer) {
        return transformer.getWelcome();
    }
}
