package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModlogCommand extends Command {

    public ModlogCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Modlog Command";
    }

    @Override
    public String getDescription() {
        return "Displays the modlogging status for the server if no arguments is given, you can also mention a text channel to enable modlogging and set it to the mentioned channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current state of the modlog module for the server.",
            "`:command <channel>` - Enabled modlogging and sets it to the mentioned channel.",
            "`:command disable` - Disables the modlogging module for the server."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command` - ",
            "`:command #modlog` - Enables modlogging and sets it to the modlog channel.",
            "`:command disable` - Disables modlogging for the server."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            ModlogHistoryCommand.class,
            ModlogReasonCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("modlog");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:user,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context,
                "Something went wrong while trying to get the guild transformer object, please contact one of my developers to look into this issue."
            );
        }

        if (args.length == 0) {
            sendModlogChannel(context, transformer).queue();
            return true;
        }

        if (ComparatorUtil.isFuzzyFalse(args[0])) {
            return disableModlog(context, transformer);
        }

        Channel channel = MentionableUtil.getChannel(context.getMessage(), args);
        if (channel == null || !(channel instanceof TextChannel)) {
            return sendErrorMessage(context, "Invalid channel argument given, you must mention a valid text channel");
        }

        if (!((TextChannel) channel).canTalk() || !context.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
            return sendErrorMessage(context, "I can't send embed messages in the specified channel, please change my permission level for the {0} channel if you want to use it as a modlog channel.", ((TextChannel) channel).getAsMention());
        }

        try {
            updateModlog(transformer, context, channel.getId());

            context.makeSuccess(":user **Server Modlogging** is now enabled and set to the :modlog channel.")
                .set("modlog", ((TextChannel) channel).getAsMention())
                .queue();
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }

        return true;
    }

    private boolean disableModlog(CommandMessage context, GuildTransformer transformer) {
        try {
            updateModlog(transformer, context, null);

            context.makeSuccess(":user **Server Modlogging** is now disabled for the server.")
                .queue();
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }

        return true;
    }

    private PlaceholderMessage sendModlogChannel(CommandMessage context, GuildTransformer transformer) {
        if (transformer.getModlog() == null) {
            return context.makeWarning(":user **Server Modlogging** is currently disabled.");
        }

        TextChannel modlogChannel = context.getGuild().getTextChannelById(transformer.getModlog());
        if (modlogChannel == null) {
            try {
                updateModlog(transformer, context, null);
            } catch (SQLException ex) {
                AvaIre.getLogger().error(ex.getMessage(), ex);
            }
            return context.makeInfo(":user **Server Modlogging** is currently disabled.");
        }

        return context.makeSuccess(":user **Server Modlogging** is currently enabled and set to the :modlog channel.")
            .set("modlog", modlogChannel.getAsMention());
    }

    private void updateModlog(GuildTransformer transformer, CommandMessage context, String value) throws SQLException {
        transformer.setModlog(value);
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", context.getGuild().getId())
            .update(statement -> statement.set("modlog", value));
    }
}
