package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class ChannelModuleCommand extends Command {

    private static final Pattern colorRegEx = Pattern.compile("^[0-9A-F]{6}$");

    public ChannelModuleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:channel,1,5"
        );
    }

    protected boolean handleEmbedOption(CommandMessage context, String[] args, GuildTransformer guildTransformer, ChannelTransformer channelTransformer, Supplier<Boolean> callback) {
        getChannelModule(channelTransformer).setEmbedColor(null);

        if (args.length > 1) {
            String color = args[1].toUpperCase();
            if (color.startsWith("#")) {
                color = color.substring(1);
            }

            if (!colorRegEx.matcher(color).matches()) {
                return sendErrorMessage(context, "Invalid color value given, the color must be a valid HEX value.\nYou can try this [random color picker](https://www.webpagefx.com/web-design/random-color-picker/) to try and get a random HEX color.");
            }

            getChannelModule(channelTransformer).setEmbedColor("#" + color);
        }

        return updateDatabase(context, guildTransformer, callback);
    }

    protected boolean updateDatabase(CommandMessage context, GuildTransformer guildTransformer, Supplier<Boolean> callback) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", context.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            return callback.get();
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }
    }

    protected boolean sendExampleMessage(CommandMessage context, User user, ChannelTransformer transformer, String defaultValue) {
        String message = StringReplacementUtil.parseGuildJoinLeaveMessage(
            context.getGuild(), context.getChannel(), user,
            getChannelModule(transformer).getMessage() == null ?
                defaultValue : getChannelModule(transformer).getMessage()
        );

        String embedColor = getChannelModule(transformer).getEmbedColor();
        if (embedColor == null) {
            context.getMessageChannel().sendMessage(message).queue();
            return true;
        }

        context.getMessageChannel().sendMessage(
            MessageFactory.createEmbeddedBuilder()
                .setDescription(message)
                .setColor(Color.decode(embedColor))
                .build()
        ).queue();

        return true;
    }

    protected boolean sendEnableMessage(CommandMessage context, ChannelTransformer channelTransformer, String type) {
        context.makeSuccess(String.join("\n",
            "The `:type` module message has been set to:",
            "",
            "```:message```",
            "",
            "You can test the message by using the command again and mentioning a user.",
            "`:command <user>`"
        ))
            .set("type", type)
            .set("message", getChannelModule(channelTransformer).getMessage())
            .set("command", generateCommandTrigger(context.getMessage()))
            .queue();

        return true;
    }

    public abstract ChannelTransformer.MessageModule getChannelModule(ChannelTransformer transformer);
}
