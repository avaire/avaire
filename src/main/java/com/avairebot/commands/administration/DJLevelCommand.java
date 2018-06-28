package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.audio.DJGuildLevel;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DJLevelCommand extends Command {

    public DJLevelCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "DJ Level Command";
    }

    @Override
    public String getDescription() {
        return "Change the DJ level requirement for the server, this changes what music commands people can use with or without the `DJ` Discord role.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("djlevel");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current DJ Level for the server.",
            "`:command types` - Displays all the types and some info about them.",
            "`:command <type>` - Change the DJ Level to the given type."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command types` - Displays all the types and info about them.",
            "`:command normal` - Changes the DJ Level to \"normal\"."
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:guild,1,4"
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
            context.makeInfo(getLevelInformation(transformer.getDJLevel()))
                .setTitle("Current DJ Level: " + transformer.getDJLevel().getName())
                .setFooter(String.format(
                    "Use \"%s types\" to see a full list of the available DJ level types.",
                    generateCommandTrigger(context.getMessage())
                ))
                .queue();

            return true;
        }

        if (args[0].equalsIgnoreCase("type") || args[0].equalsIgnoreCase("types")) {
            PlaceholderMessage placeholderMessage = MessageFactory.makeEmbeddedMessage(context.getChannel());

            for (DJGuildLevel level : DJGuildLevel.values()) {
                placeholderMessage.addField(level.getName(), getLevelInformation(level), false);
            }

            placeholderMessage
                .setTitle("DJ Level Types")
                .setFooter(String.format(
                    "Use \"%s <type>\" to change the DJ Level to the given type for the server.",
                    generateCommandTrigger(context.getMessage())
                ))
                .queue();

            return true;
        }

        DJGuildLevel level = DJGuildLevel.fromName(args[0]);
        if (level == null) {
            return sendErrorMessage(context, "`{0}` is not a valid `DJ Level` type, please use one of the following:\n`{1}`",
                args[0], String.join("`, `", DJGuildLevel.getNames())
            );
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("dj_level", level.getId()));
            transformer.setDJLevel(level);

            context.makeSuccess("The `DJ Level` status has changed to **:type**.\n:info")
                .set("type", level.getName())
                .set("info", getLevelInformation(level))
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
            AvaIre.getLogger().error(e.getMessage(), e);
        }

        return false;
    }

    private String getLevelInformation(DJGuildLevel level) {
        switch (level) {
            case ALL:
                return "Anyone can run any music commands, even without the `DJ` role.";

            case NONE:
                return "No one can run any music commands without the `DJ` role.";

            default:
                return "Preventing people from using commands like playlists, volume control, and force skip without the `DJ` role, but still allowing people to use the play command without the role.";
        }
    }
}
