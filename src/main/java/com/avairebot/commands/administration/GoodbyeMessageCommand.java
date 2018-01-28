package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@CacheFingerprint(name = "welcome-goodbye-message-command")
public class GoodbyeMessageCommand extends Command {

    public GoodbyeMessageCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Goodbye Message Command";
    }

    @Override
    public String getDescription() {
        return "Sets the message that should be sent when a user leaves the server, this command can only be used if the goodbye module is enabled for the current channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Resets the goodbye back to the default message.",
            "`:command <message>` - Sets the goodbye message to the given message.",
            "`:command <user>` - If a valid username, nickname or user was mentioned, an example message will be sent for the given user."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("goodbyemessage", "byemsg");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:channel,1,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(avaire, message);
        ChannelTransformer channelTransformer = guildTransformer.getChannel(message.getTextChannel().getId());

        if (channelTransformer == null || !channelTransformer.getGoodbye().isEnabled()) {
            return sendErrorMessage(message, "The `goodbye` module must be enabled to use this command, you can enable the `goodbye` module by using the `.goodbye` command.");
        }

        if (args.length == 1) {
            User user = MentionableUtil.getUser(message, args, 0);

            if (user != null) {
                return sendExampleMessage(message, user, channelTransformer);
            }
        }

        channelTransformer.getGoodbye().setMessage(args.length == 0 ? null : String.join(" ", args));

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", message.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            if (channelTransformer.getGoodbye().getMessage() == null) {
                MessageFactory.makeSuccess(message, "The `Goodbye` module message has been set back to the default.").queue();

                return true;
            }

            return sendEnableMessage(message, channelTransformer);
        } catch (SQLException ex) {
            AvaIre.getLogger().error("Failed to update the goodbye message", ex);

            MessageFactory.makeError(message, "Failed to save the guild settings: :error")
                .set("error", ex.getMessage())
                .queue();
            return false;
        }
    }

    private boolean sendExampleMessage(Message message, User user, ChannelTransformer transformer) {
        message.getChannel().sendMessage(StringReplacementUtil.parseGuildJoinLeaveMessage(
            message.getGuild(), message.getTextChannel(), user,
            transformer.getGoodbye().getMessage() == null ?
                "%user% has left **%server%**! :(" :
                transformer.getGoodbye().getMessage())
        ).queue();

        return true;
    }

    private boolean sendEnableMessage(Message message, ChannelTransformer channelTransformer) {
        MessageFactory.makeSuccess(message, String.join("\n",
            "The `Welcome` module message has been set to:",
            "",
            "```:message```",
            "",
            "You can test the message by using the command again and mentioning a user.",
            "`:command <user>`"
        ))
            .set("message", channelTransformer.getWelcome().getMessage())
            .set("command", generateCommandTrigger(message))
            .queue();

        return true;
    }
}
