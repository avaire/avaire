package com.avairebot.commands.utility;

import com.avairebot.AppInfo;
import com.avairebot.AvaIre;
import com.avairebot.Statistics;
import com.avairebot.audio.AudioHandler;
import com.avairebot.cache.CacheType;
import com.avairebot.chat.MessageType;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.NumberUtil;
import com.google.gson.internal.LinkedTreeMap;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatsCommand extends Command {

    private final DecimalFormat decimalNumber;

    public StatsCommand(AvaIre avaire) {
        super(avaire);

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');

        decimalNumber = new DecimalFormat("#,##0.00", decimalFormatSymbols);
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
    public List<String> getTriggers() {
        return Arrays.asList("stats", "about");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        StringBuilder description = new StringBuilder("Created by [Senither#0001](https://senither.com/) using the [JDA](https://github.com/DV8FromTheWorld/JDA) framework!");
        if (avaire.getCache().getAdapter(CacheType.FILE).has("github.commits")) {
            description = new StringBuilder("**Latest changes:**\n");
            List<LinkedTreeMap<String, Object>> items = (List<LinkedTreeMap<String, Object>>) avaire.getCache().getAdapter(CacheType.FILE).get("github.commits");

            for (int i = 0; i < 3; i++) {
                LinkedTreeMap<String, Object> item = items.get(i);
                LinkedTreeMap<String, Object> commit = (LinkedTreeMap<String, Object>) item.get("commit");

                description.append(String.format("[`%s`](%s) %s\n",
                    item.get("sha").toString().substring(0, 7),
                    item.get("html_url"),
                    commit.get("message").toString().split("\n")[0].trim()
                ));
            }
        }

        MessageFactory.makeEmbeddedMessage(message.getChannel(), MessageType.INFO,
            new MessageEmbed.Field("Author", "Senither#0001", true),
            new MessageEmbed.Field("Bot ID", message.getJDA().getSelfUser().getId(), true),
            new MessageEmbed.Field("Library", "[JDA](https://github.com/DV8FromTheWorld/JDA)", true),
            new MessageEmbed.Field("DB Queries run", getDatabaseQueriesStats(), true),
            new MessageEmbed.Field("Messages Received", getMessagesReceivedStats(), true),
            new MessageEmbed.Field("Shard", "" + message.getJDA().getShardInfo().getShardId(), true),
            new MessageEmbed.Field("Commands Ran", NumberUtil.formatNicely(Statistics.getCommands()), true),
            new MessageEmbed.Field("Memory Usage", memoryUsage(), true),
            new MessageEmbed.Field("Uptime", applicationUptime(), true),
            new MessageEmbed.Field("Members", NumberUtil.formatNicely(avaire.getShardEntityCounter().getUsers()), true),
            new MessageEmbed.Field("Channels", NumberUtil.formatNicely(avaire.getShardEntityCounter().getChannels()), true),
            new MessageEmbed.Field("Servers", NumberUtil.formatNicely(avaire.getShardEntityCounter().getGuilds()), true)
        )
            .setTitle("Official Bot Server Invite", "https://discordapp.com/invite/gt2FWER")
            .setAuthor("AvaIre v" + AppInfo.getAppInfo().VERSION, "https://discordapp.com/invite/gt2FWER", avaire.getSelfUser().getEffectiveAvatarUrl())
            .setFooter(String.format("Currently playing in %s servers with %s songs in the queue.",
                NumberUtil.formatNicely(AudioHandler.getTotalListenersSize()),
                NumberUtil.formatNicely(AudioHandler.getTotalQueueSize())
            ))
            .setDescription(description.toString())
            .queue();

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

    private String getDatabaseQueriesStats() {
        return String.format("%s (%s per min)",
            NumberUtil.formatNicely(Statistics.getQueries()),
            decimalNumber.format(Statistics.getQueries() / ((double) ManagementFactory.getRuntimeMXBean().getUptime() / (double) (1000 * 60)))
        );
    }

    private String getMessagesReceivedStats() {
        return String.format("%s (%s per sec)",
            NumberUtil.formatNicely(Statistics.getMessages()),
            decimalNumber.format(Statistics.getMessages() / ((double) ManagementFactory.getRuntimeMXBean().getUptime() / (double) (1000)))
        );
    }

    private String memoryUsage() {
        return String.format("%sMb / %sMb",
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
            Runtime.getRuntime().totalMemory() / (1024 * 1024)
        );
    }
}
