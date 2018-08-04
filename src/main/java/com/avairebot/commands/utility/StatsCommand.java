package com.avairebot.commands.utility;

import com.avairebot.AppInfo;
import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.cache.CacheType;
import com.avairebot.chat.MessageType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.language.I18n;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.NumberUtil;
import com.google.gson.internal.LinkedTreeMap;
import io.prometheus.client.Collector;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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
        return "Displays information about Ava and some related stats.";
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
    public boolean onCommand(CommandMessage context, String[] args) {
        StringBuilder description = new StringBuilder("Created by [Senither#0001](https://senither.com/) using the [JDA](https://github.com/DV8FromTheWorld/JDA) framework!");
        if (avaire.getCache().getAdapter(CacheType.FILE).has("github.commits")) {
            description = new StringBuilder("**" + context.i18n("latestChanges") + "**\n");
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

        context.makeEmbeddedMessage(MessageType.INFO,
            new MessageEmbed.Field(context.i18n("fields.author"), "Senither#0001", true),
            new MessageEmbed.Field(context.i18n("fields.botId"), context.getJDA().getSelfUser().getId(), true),
            new MessageEmbed.Field(context.i18n("fields.library"), "[JDA](https://github.com/DV8FromTheWorld/JDA)", true),
            new MessageEmbed.Field(context.i18n("fields.database"), getDatabaseQueriesStats(context), true),
            new MessageEmbed.Field(context.i18n("fields.messages"), getMessagesReceivedStats(context), true),
            new MessageEmbed.Field(context.i18n("fields.shard"), "" + context.getJDA().getShardInfo().getShardId(), true),
            new MessageEmbed.Field(context.i18n("fields.commands"), NumberUtil.formatNicely(getTotalsFrom(Metrics.commandsReceived.collect())), true),
            new MessageEmbed.Field(context.i18n("fields.memory"), memoryUsage(context), true),
            new MessageEmbed.Field(context.i18n("fields.uptime"), applicationUptime(), true),
            new MessageEmbed.Field(context.i18n("fields.members"), NumberUtil.formatNicely(avaire.getShardEntityCounter().getUsers()), true),
            new MessageEmbed.Field(context.i18n("fields.channels"), NumberUtil.formatNicely(avaire.getShardEntityCounter().getChannels()), true),
            new MessageEmbed.Field(context.i18n("fields.servers"), NumberUtil.formatNicely(avaire.getShardEntityCounter().getGuilds()), true)
        )
            .setTitle(context.i18n("title"), "https://discordapp.com/invite/gt2FWER")
            .setAuthor("AvaIre v" + AppInfo.getAppInfo().version, "https://discordapp.com/invite/gt2FWER", avaire.getSelfUser().getEffectiveAvatarUrl())
            .setFooter(context.i18n("footer",
                NumberUtil.formatNicely(AudioHandler.getDefaultAudioHandler().getTotalListenersSize()),
                NumberUtil.formatNicely(AudioHandler.getDefaultAudioHandler().getTotalQueueSize())
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

    private String getDatabaseQueriesStats(CommandMessage context) {
        return formatDynamicValue(context, getTotalsFrom(Metrics.databaseQueries.collect()));
    }

    private String getMessagesReceivedStats(CommandMessage context) {
        return formatDynamicValue(context, (int) Metrics.jdaEvents.labels(MessageReceivedEvent.class.getSimpleName()).get());
    }

    private String memoryUsage(CommandMessage context) {
        return context.i18n("formats.memory",
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
            Runtime.getRuntime().totalMemory() / (1024 * 1024)
        );
    }

    private String formatDynamicValue(CommandMessage context, int rawValue) {
        double value = rawValue / ((double) ManagementFactory.getRuntimeMXBean().getUptime() / 1000D);
        return I18n.format(
            context.i18n(value < 1.5D ? "formats.perMinute" : "formats.perSecond"),
            NumberUtil.formatNicely(rawValue),
            decimalNumber.format(value < 1.5D ? value * 60D : value)
        );
    }

    private int getTotalsFrom(List<Collector.MetricFamilySamples> familySamples) {
        double total = 0.0D;
        for (Collector.MetricFamilySamples family : familySamples) {
            for (Collector.MetricFamilySamples.Sample sample : family.samples) {
                total += sample.value;
            }
        }
        return (int) total;
    }
}
