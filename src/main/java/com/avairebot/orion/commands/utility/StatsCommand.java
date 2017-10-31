package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.chat.MessageType;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.google.gson.internal.LinkedTreeMap;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatsCommand extends Command {

    public StatsCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Stats Command";
    }

    @Override
    public String getDescription() {
        return "Tells you information about the bot itself.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Shows some stats about the bot.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("stats", "about");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        Guild guild = message.getGuild();

        StringBuilder description = new StringBuilder("Created by [Senither#8023](https://senither.com/) using the [JDA](https://github.com/DV8FromTheWorld/JDA) framework!");
        if (orion.cache.getAdapter(CacheType.FILE).has("github.commits")) {
            description = new StringBuilder("**Latest changes:**\n");
            List<LinkedTreeMap<String, Object>> items = (List<LinkedTreeMap<String, Object>>) orion.cache.getAdapter(CacheType.FILE).get("github.commits");

            for (int i = 0; i < 3; i++) {
                LinkedTreeMap<String, Object> item = items.get(i);
                LinkedTreeMap<String, Object> commit = (LinkedTreeMap<String, Object>) item.get("commit");

                description.append(String.format("[`%s`](%s) %s\n",
                    item.get("sha").toString().substring(0, 7),
                    item.get("html_url"),
                    commit.get("message")
                ));
            }
        }

        message.getChannel().sendMessage(
            MessageFactory.makeEmbeddedMessage(MessageType.INFO,
                new MessageEmbed.Field("Author", "Senither#8023", true),
                new MessageEmbed.Field("Bot ID", message.getJDA().getSelfUser().getId(), true),
                new MessageEmbed.Field("Library", "[JDA](https://github.com/DV8FromTheWorld/JDA)", true),
                new MessageEmbed.Field("DB Queries run", "" + Statistics.getQueries(), true),
                new MessageEmbed.Field("Messages Received", "" + Statistics.getMessages(), true),
                new MessageEmbed.Field("Shard", "Unknown", true),
                new MessageEmbed.Field("Commands Run", "" + Statistics.getCommands(), true),
                new MessageEmbed.Field("Memory Usage", "Unknown", true),
                new MessageEmbed.Field("Uptime", "" + applicationUptime(), true),
                new MessageEmbed.Field("Members", "" + orion.getJDA().getUsers().size(), true),
                new MessageEmbed.Field("Channels", "" + (orion.getJDA().getTextChannels().size() + orion.getJDA().getVoiceChannels().size()), true),
                new MessageEmbed.Field("Servers", "" + message.getJDA().getGuilds().size(), true)
            )
                .setTitle("Official Bot Server Invite", "https://discordapp.com/invite/gt2FWER")
                .setAuthor("Orion v" + orion.getVersion(), "https://discordapp.com/invite/gt2FWER", orion.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setDescription(description.toString())
                .build()
        ).queue();

        return true;
    }

    private String applicationUptime() {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long seconds = rb.getUptime() / 1000;
        long d = (long) Math.floor(seconds / 86400);
        long h = (long) Math.floor((seconds % 86400) / 3600);
        long m = (long) Math.floor(((seconds % 86400) % 3600) / 60);
        long s = (long) Math.floor(((seconds % 86400) % 3600) % 60);

        if (d > 0) {
            return String.format("%sd %sh %sm %ss", d, h, m, s);
        }

        if (h > 0) {
            return String.format("%sh %sm %ss", h, m, s);
        }

        if (m > 0) {
            return String.format("%sm %ss", m, s);
        }
        return String.format("%ss", s);
    }
}
