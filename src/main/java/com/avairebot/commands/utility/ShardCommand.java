package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.shard.AvaireShard;
import net.dv8tion.jda.core.entities.Message;

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
        return "Displays the status of all the shards for the bot, including their server count, channel count, user count and latency."
            + ((avaire.getShards().size() < 2) ? "\n**Shards are currently disabled: This command will just run the stats command.**" : "");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("shards", "shard");
    }

    @Override
    public CommandPriority getCommandPriority() {
        if (avaire.getShards().size() < 2) {
            return CommandPriority.HIDDEN;
        }
        return super.getCommandPriority();
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (avaire.getShards().size() < 2) {
            CommandContainer container = CommandHandler.getCommand(StatsCommand.class);
            if (container == null) {
                return sendErrorMessage(message, "Sharding is not enabled right now :(");
            }
            return container.getCommand().onCommand(message, args);
        }

        List<String> messages = new ArrayList<>();
        messages.add("**All Shards**```prolog");

        for (AvaireShard shard : avaire.getShards()) {
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
            avaire.getShardEntityCounter().getGuilds(),
            avaire.getShardEntityCounter().getChannels(),
            avaire.getShardEntityCounter().getUsers()
        ));
        messages.add("```");

        MessageFactory.makeInfo(message, String.join("\n", messages)).queue();
        return true;
    }
}
