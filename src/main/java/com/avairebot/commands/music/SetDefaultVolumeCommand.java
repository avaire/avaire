package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.NumberUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetDefaultVolumeCommand extends Command {

    public SetDefaultVolumeCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Set Default Volume Command";
    }

    @Override
    public String getDescription(CommandContext context) {
        String prefix = context.isGuildMessage() ? generateCommandPrefix(context.getMessage()) : DiscordConstants.DEFAULT_COMMAND_PREFIX;

        return String.format(String.join("\n", Arrays.asList(
            "Sets the default volume that the music should play at when Ava first joins a voice channel.",
            "**Note:** This does not change the volume of music already playing, to change that, use the `%svolume` command instead."
        )), prefix);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("default-volume", "set-volume");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current default volume",
            "`:command <volume>` - Changes the default volume to the given volume."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command 75` - Sets the default volume to 75"
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,1,4",
            "hasVoted",
            "musicChannel"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "default volume");
        }

        if (args.length == 0) {
            return sendCurrentVolume(context, transformer);
        }

        int vol = NumberUtil.parseInt(args[0], 0);
        if (vol < 10 || vol > 100) {
            return sendErrorMessage(context, context.i18n("mustBeNumber"));
        }

        transformer.setDefaultVolume(vol);

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("default_volume", vol));

            context.makeSuccess(context.i18n("changedVolume"))
                .set("volume", vol)
                .queue();

            return true;
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to store the default volume in the database due to a SQLException: ", e);
            context.makeError(context.i18n("failedToSave", e.getMessage())).queue();
        }

        return false;
    }

    private boolean sendCurrentVolume(CommandMessage context, GuildTransformer transformer) {
        context.makeSuccess(context.i18n("currentVolume"))
            .set("volume", transformer.getDefaultVolume())
            .queue();

        return false;
    }
}
