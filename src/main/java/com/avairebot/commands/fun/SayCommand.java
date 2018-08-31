package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collections;
import java.util.List;

public class SayCommand extends Command {

    public SayCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Say Command";
    }

    @Override
    public String getDescription() {
        return "I will say whatever you tell me to.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <message> <optional:channelName>` - Makes the bot say the given message. Can specify the channel for the message to be said, defaults to current channel.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command I am a BOT`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("say");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("require:all,text.manage_messages");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }
        if (args[2] != null) {
            List<TextChannel> channelsToSendMessage = context.guild.getTextChannelsByName(args[2], true);
            if (!channelsToSendMessage.isEmpty()) {
                TextChannel channel = channelsToSendMessage.get(0);
                channel.sendMessage(context.getContentRaw()).queue();
            } else {
                context.getMessageChannel().sendMessage(context.getContentRaw()).queue();
            }
        } else {
            context.getMessageChannel().sendMessage(context.getContentRaw()).queue();
        }

        if (context.isGuildMessage()) {
            context.delete().reason("AvaIre say command usage").queue(null, RestActionUtil.ignore);
        }

        return true;
    }
}
