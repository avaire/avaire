package com.avairebot.logger;

import com.avairebot.AvaIre;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EventLogger {

    private static final List<MessageEmbed> GUILD_MESSAGES = new ArrayList<>();

    public static void logGuildJoin(AvaIre avaire, GuildJoinEvent event) {
        if (!isWebhookEnabled(avaire)) {
            return;
        }

        User owner = event.getGuild().getOwner().getUser();

        GUILD_MESSAGES.add(new EmbedBuilder()
            .setColor(Color.decode("#66BB6A"))
            .setTimestamp(Instant.now())
            .addField("Added", String.format("%s (ID: %s)",
                event.getGuild().getName(), event.getGuild().getId()
            ), false)
            .addField("Owner", String.format("%s#%s (ID: %s)",
                owner.getName(), owner.getDiscriminator(), owner.getId()
            ), false)
            .build()
        );
    }

    public static void logGuildLeave(AvaIre avaire, GuildLeaveEvent event) {
        if (!isWebhookEnabled(avaire)) {
            return;
        }

        GUILD_MESSAGES.add(new EmbedBuilder()
            .setColor(Color.decode("#EF5350"))
            .setTimestamp(Instant.now())
            .addField("Removed", String.format("%s (ID: %s)",
                event.getGuild().getName(), event.getGuild().getId()
            ), false)
            .build()
        );
    }

    public static List<MessageEmbed> getGuildMessages() {
        return GUILD_MESSAGES;
    }

    public static List<MessageEmbed> pullGuildMessages() {
        ArrayList<MessageEmbed> messageEmbeds = new ArrayList<>(GUILD_MESSAGES);
        GUILD_MESSAGES.clear();
        return messageEmbeds;
    }

    private static boolean isWebhookEnabled(AvaIre avaire) {
        return avaire.getConfig().getString("webhook.id", "").trim().length() > 0 &&
            avaire.getConfig().getString("webhook.token", "").trim().length() > 0;
    }
}
