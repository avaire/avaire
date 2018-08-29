package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.ComparatorUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MusicMessagesCommand extends Command {

    public MusicMessagesCommand(AvaIre plugin) {
        super(plugin, false);
    }

    @Override
    public String getName() {
        return "Music Messages Command";
    }

    @Override
    public String getDescription() {
        return "Toggles music messages on and off, when music messages are off, the \"Now Playing\" messages will no longer be sent, and messages that before would stay, will now be automatically be deleted after awhile. ";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current music message status.",
            "`:command [on|off]` - Toggles the music messages on/off."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command off` - Turns off music messages.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("musicmessages", "musicmessage");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (context.getGuildTransformer() == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "music messages");
        }

        if (args.length == 0) {
            return displayMusicMessageStatus(context);
        }

        ComparatorUtil.ComparatorType type = ComparatorUtil.getFuzzyType(args[0]);
        if (type.equals(ComparatorUtil.ComparatorType.UNKNOWN)) {
            return sendErrorMessage(context, context.i18n("error"));
        }

        if (type.getValue() == context.getGuildTransformer().isMusicMessages()) {
            return sendSuccessMessage(context, type);
        }

        try {
            context.getGuildTransformer().setMusicMessages(type.getValue());

            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("music_messages", type.getValue()));

            return sendSuccessMessage(context, type);
        } catch (SQLException e) {
            // Since we're running the query as async we won't really need to
            // check for errors since that is handled by the database thread.
        }

        return false;
    }

    private boolean sendSuccessMessage(CommandMessage context, ComparatorUtil.ComparatorType type) {
        context.makeSuccess(context.i18n("updated"))
            .set("status", type.getValue()
                ? context.i18n("status.enabled")
                : context.i18n("status.disabled")
            )
            .queue();

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean displayMusicMessageStatus(CommandMessage context) {
        boolean status = context.getGuildTransformer().isMusicMessages();

        context.makeInfo(context.i18n("message"))
            .setFooter(context.i18n("footer",
                generateCommandTrigger(context.getMessage()) + " [on|off]"
            ))
            .set("status", status
                ? context.i18n("status.enabled")
                : context.i18n("status.disabled")
            )
            .set("info", status
                ? context.i18n("info.enabled")
                : context.i18n("info.disabled")
            )
            .queue();

        return true;
    }
}
