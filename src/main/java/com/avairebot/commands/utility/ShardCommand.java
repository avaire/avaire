package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
import net.dv8tion.jda.core.JDA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShardCommand extends Command {

    public ShardCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Shard Command";
    }

    @Override
    public String getDescription() {
        // If the shard manager is null, we're still starting up the bot.
        if (avaire.getShardManager() == null) {
            return "If you're seeing this message, contact one of the bot developers.";
        }

        return "Displays the status of all the shards for the bot, including their server count, channel count, user count and latency."
            + ((avaire.getShardManager().getShards().size() < 2) ? "\n**Shards are currently disabled: This command will just run the stats command.**" : "");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("shards", "shard");
    }

    @Override
    public CommandPriority getCommandPriority() {
        if (avaire.getShardManager().getShards().size() < 2) {
            return CommandPriority.HIDDEN;
        }
        return super.getCommandPriority();
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (avaire.getShardManager().getShards().size() < 2) {
            CommandContainer container = CommandHandler.getCommand(StatsCommand.class);
            if (container == null) {
                return sendErrorMessage(context, "Sharding is not enabled right now :(");
            }
            context.setI18nCommandPrefix(container);
            return container.getCommand().onCommand(context, args);
        }

        List<String> messages = new ArrayList<>();
        messages.add("**" + context.i18n("allShards") + "**```prolog");

        for (int i = 0; i < avaire.getShardManager().getShardsTotal(); i++) {
            JDA shard = avaire.getShardManager().getShardById(i);
            messages.add(String.format("%s : G %s, C %s, U %s, L %s%s",
                shard.getShardInfo().getShardId(),
                shard.getGuilds().size(),
                shard.getTextChannels().size() + shard.getVoiceChannels().size(),
                shard.getUsers().size(),
                shard.getPing(),
                shard.getShardInfo().getShardId() == context.getJDA().getShardInfo().getShardId() ? " <--" : ""
            ));
        }
        messages.add("```");

        messages.add("**" + context.i18n("totalShards") + "**```ml");
        messages.add(String.format("G %s, C %s, U %s, L %s",
            avaire.getShardEntityCounter().getGuilds(),
            avaire.getShardEntityCounter().getChannels(),
            avaire.getShardEntityCounter().getUsers(),
            avaire.getShardManager().getAveragePing()
        ));
        messages.add("```");

        context.makeInfo(String.join("\n", messages)).queue();
        return true;
    }
}
