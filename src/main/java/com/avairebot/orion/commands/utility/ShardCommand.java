package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.CommandPriority;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.shard.OrionShard;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShardCommand extends Command {

    public ShardCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Shard Command";
    }

    @Override
    public String getDescription() {
        return "Displays the status of all the shards for the bot, including their server count, channel count, user count and latency."
            + ((orion.getShards().size() < 2) ? "\n**Shards are currently disabled: This command will just run the stats command.**" : "");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("shards", "shard");
    }

    @Override
    public CommandPriority getCommandPriority() {
        if (orion.getShards().size() < 2) {
            return CommandPriority.HIDDEN;
        }
        return super.getCommandPriority();
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (orion.getShards().size() < 2) {
            CommandContainer container = CommandHandler.getCommand(StatsCommand.class);
            if (container == null) {
                return sendErrorMessage(message, "Sharding is not enabled right now :(");
            }
            return container.getCommand().onCommand(message, args);
        }

        List<String> messages = new ArrayList<>();
        messages.add("**All Shards**```prolog");

        for (OrionShard shard : orion.getShards()) {
            messages.add(String.format("%s : G %s, C %s, U %s%s",
                shard.getShardId(),
                shard.getJDA().getGuilds().size(),
                shard.getJDA().getTextChannels().size() + shard.getJDA().getVoiceChannels().size(),
                shard.getJDA().getUsers().size(),
                shard.getShardId() == message.getJDA().getShardInfo().getShardId() ? " <--" : ""
            ));
        }
        messages.add("```");

        messages.add("**Total Shards**```ml");
        messages.add(String.format("G %s, C %s, U %s",
            orion.getShardEntityCounter().getGuilds(),
            orion.getShardEntityCounter().getChannels(),
            orion.getShardEntityCounter().getUsers()
        ));
        messages.add("```");

        MessageFactory.makeInfo(message, String.join("\n", messages)).queue();
        return true;
    }
}
