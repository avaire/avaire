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

package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.interactions.Lottery;
import com.avairebot.language.I18n;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.CacheUtil;
import com.avairebot.utilities.MentionableUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class InteractionCommand extends Command {

    /**
     * The Guava cache instance, used for caching the sent messages, and
     * helps determine if the response message should be sent or not.
     *
     * @see Metrics#setup(AvaIre) Metrics setup.
     */
    public static final Cache<String, Lottery> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    /**
     * Creates a new interaction command instance.
     *
     * @param avaire The main {@link AvaIre avaire} application instance.
     */
    public InteractionCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getDescription(@Nullable CommandContext context) {
        return String.format(
            "Sends the **%s** interaction to the mentioned user.",
            getInteraction(context, true)
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <target>`",
            "`:command <user> <target>`"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command @AvaIre`",
            "`:command @AvaIre @Senither`",
            "`:command @Someone @Me`"
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.INTERACTIONS);
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIGH;
    }

    /**
     * Gets the colour that should be used for the ebbed message when
     * he interaction message is sent, by default this will return
     * {@code NULL} which will not set any colour.
     *
     * @return The colour that should be used for the embed message.
     */
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public Color getInteractionColor() {
        return null;
    }

    /**
     * Gets the list of interaction images that can be returned by the
     * interaction, using the image full URL to where it is hosted.
     * <p>
     * Example: https://i.imgur.com/ZupgGkI.jpg
     *
     * @return A list of interaction images.
     */
    @Nonnull
    public abstract List<String> getInteractionImages();

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = context.getAuthor();
        User target = MentionableUtil.getUser(context, args, 0);
        if (target == null) {
            return sendErrorMessage(context, "You must mention a use you want to use the interaction for.");
        }

        User secondaryTarget = MentionableUtil.getUser(context, args, 1);
        if (secondaryTarget != null) {
            user = target;
            target = secondaryTarget;
        }

        handleCommandIndication(context);

        List<String> interactionImages = getInteractionImages();

        Lottery lottery = (Lottery) CacheUtil.getUncheckedUnwrapped(cache, asKey(context),
            () -> new Lottery(interactionImages.size())
        );

        int imageIndex = lottery.getWinner();

        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = context.makeEmbeddedMessage()
            .setImage("attachment://" + getClass().getSimpleName() + "-" + imageIndex + ".gif")
            .setDescription(buildMessage(context, user, target))
            .setColor(getInteractionColor())
            .requestedBy(context)
            .build();

        messageBuilder.setEmbed(embedBuilder.build());

        try {
            InputStream stream = new URL(interactionImages.get(imageIndex)).openStream();

            context.getChannel().sendFile(stream, getClass().getSimpleName() + "-" + imageIndex + ".gif", messageBuilder.build()).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String buildMessage(CommandMessage context, User user, User target) {
        return I18n.format(
            getInteraction(context, false),
            context.getGuild().getMember(user).getEffectiveName(),
            context.getGuild().getMember(target).getEffectiveName()
        );
    }

    private void handleCommandIndication(CommandMessage context) {
        if (!context.getGuild().getSelfMember().hasPermission(context.getChannel(), Permission.MESSAGE_MANAGE)) {
            context.getChannel().sendTyping().queue();
            return;
        }

        context.getMessage().delete().queue(null, error -> {
            context.getChannel().sendTyping().queue();
        });
    }

    private String getInteraction(@Nullable CommandContext context, boolean isDescription) {
        if (isDescription) {
            return getTriggers().get(0);
        }

        if (context != null) {
            return context.i18nRaw(context.getI18nCommandPrefix());
        }

        CommandContainer container = CommandHandler.getCommand(this);
        if (container == null) {
            return "Unknown";
        }

        return I18n.getString(null,
            container.getCategory().getName().toLowerCase() + "."
                + container.getCommand().getClass().getSimpleName()
        );
    }

    private String asKey(CommandContext context) {
        return getClass().getSimpleName() + "." + context.getGuild().getIdLong();
    }
}
