package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

public class StatsCommand extends AbstractCommand {

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
    public List<String> getTriggers() {
        return Arrays.asList("stats", "about");
    }

    @Override
    public boolean onCommand(MessageReceivedEvent event, String[] args) {
        Guild guild = event.getMessage().getGuild();
        MessageFactory.makeEmbeddedMessage(event.getMessage().getChannel(), MessageFactory.MessageType.INFO,
                new MessageEmbed.Field("Author", "Senither#8023", true),
                new MessageEmbed.Field("Bot ID", event.getJDA().getSelfUser().getId(), true),
                new MessageEmbed.Field("Library", "[JDA](https://github.com/DV8FromTheWorld/JDA)", true),
                new MessageEmbed.Field("DB Queries run", "" + Statistics.getQueries(), true),
                new MessageEmbed.Field("Messages Received", "" + Statistics.getMessages(), true),
                new MessageEmbed.Field("Shard", "Unknown", true),
                new MessageEmbed.Field("Commands Run", "" + Statistics.getCommands(), true),
                new MessageEmbed.Field("Memory Usage", "Unknown", true),
                new MessageEmbed.Field("Uptime", "" + applicationUptime(), true),
                new MessageEmbed.Field("Members", "" + guild.getMembers().size(), true),
                new MessageEmbed.Field("Channels", "" + guild.getTextChannels().size() + guild.getVoiceChannels().size(), true),
                new MessageEmbed.Field("Servers", "" + event.getJDA().getGuilds().size(), true)
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
