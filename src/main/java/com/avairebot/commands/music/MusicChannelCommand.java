/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class MusicChannelCommand extends Command {

    public MusicChannelCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Music Channel Command";
    }

    @Override
    public String getDescription() {
        return "The music channel command can be used to define a text and voice channel that music should be linked to, if a text channel is set through the command, music commands will only work in the given channel, if a voice channel is set Ava will auto join the voice channel on the first music request.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current music channels.",
            "`:command <voice|text>` - Disables the music text or voice channel.",
            "`:command <voice|text> <channel>` - Sets the music text or voice channel."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command text` - Disables the music text channel if one was set.",
            "`:command voice music` - Sets the voice music channel to the `music` channel."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("musicchannel", "mchannel");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_SETTINGS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (context.getGuildTransformer() == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "music channels");
        }

        if (args.length == 0) {
            return displayCurrentChannels(context);
        }

        switch (args[0].toLowerCase()) {
            case "v":
            case "voice":
                return handleVoice(context, Arrays.copyOfRange(args, 1, args.length));

            case "t":
            case "txt":
            case "text":
                return handleText(context, Arrays.copyOfRange(args, 1, args.length));
        }

        return sendErrorMessage(context, context.i18n("errors.invalidType"));
    }

    private boolean displayCurrentChannels(CommandMessage context) {
        TextChannel textChannel = getTextChannel(context);
        VoiceChannel voiceChannel = getVoiceChannel(context);

        context.makeInfo(context.i18n("field.message"))
            .setTitle(context.i18n("field.title"))
            .set("text", textChannel == null ? "Disabled" : textChannel.getAsMention() + " (ID: " + textChannel.getId() + ")")
            .set("voice", voiceChannel == null ? "Disabled" : voiceChannel.getName() + " (ID: " + voiceChannel.getId() + ")")
            .queue();

        return true;
    }

    private boolean handleVoice(CommandMessage context, String[] args) {
        if (args.length == 0) {
            context.getGuildTransformer().setMusicChannelVoice(null);
            updateDatabase(context, Type.VOICE, null);
            return true;
        }

        args = new String[]{String.join(" ", args)};
        Channel channel = MentionableUtil.getChannel(context.getMessage(), args, 0, MentionableUtil.ChannelPriorityType.VOICE);
        if (channel == null || !(channel instanceof VoiceChannel)) {
            return sendErrorMessage(context, context.i18n("errors.notValidType", args[0], Type.VOICE.name().toLowerCase()));
        }

        context.getGuildTransformer().setMusicChannelVoice(channel.getId());
        updateDatabase(context, Type.VOICE, channel);

        return true;
    }

    private boolean handleText(CommandMessage context, String[] args) {
        if (args.length == 0) {
            context.getGuildTransformer().setMusicChannelText(null);
            updateDatabase(context, Type.TEXT, null);
            return true;
        }

        args = new String[]{String.join(" ", args)};
        Channel channel = MentionableUtil.getChannel(context.getMessage(), args, 0, MentionableUtil.ChannelPriorityType.TEXT);
        if (channel == null || !(channel instanceof TextChannel)) {
            return sendErrorMessage(context, context.i18n("errors.notValidType", args[0], Type.TEXT.name().toLowerCase()));
        }

        context.getGuildTransformer().setMusicChannelText(channel.getId());
        updateDatabase(context, Type.TEXT, channel);

        return true;
    }

    private void updateDatabase(CommandMessage context, Type type, Channel value) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set(type.getField(), value == null ? null : value.getId()));

            String status = null;
            if (value != null) {
                status = (value instanceof TextChannel)
                    ? ((TextChannel) value).getAsMention()
                    : value.getName();
            }

            context.makeSuccess(context.i18n("updated"))
                .set("type", type.name().toLowerCase())
                .set("status", value == null
                    ? context.i18n("status.disabled")
                    : context.i18n("status.enabled", status)
                )
                .queue();
        } catch (SQLException ignored) {
            // Since we're running the query as async we won't really need to
            // check for errors since that is handled by the database thread.
        }
    }

    private TextChannel getTextChannel(CommandMessage context) {
        String channel = context.getGuildTransformer().getMusicChannelText();
        if (channel == null) {
            return null;
        }
        return context.getGuild().getTextChannelById(channel);
    }

    private VoiceChannel getVoiceChannel(CommandMessage context) {
        String channel = context.getGuildTransformer().getMusicChannelVoice();
        if (channel == null) {
            return null;
        }
        return context.getGuild().getVoiceChannelById(channel);
    }

    private enum Type {
        TEXT("music_channel_text"), VOICE("music_channel_voice");

        private final String field;

        Type(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }
}
