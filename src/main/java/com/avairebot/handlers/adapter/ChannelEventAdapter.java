package com.avairebot.handlers.adapter;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;

import java.sql.SQLException;

public class ChannelEventAdapter extends EventAdapter {

    /**
     * Instantiates the event adapter and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public ChannelEventAdapter(AvaIre avaire) {
        super(avaire);
    }

    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());
        if (transformer == null) {
            return;
        }

        if (transformer.getModlog() != null && transformer.getModlog().equalsIgnoreCase(event.getChannel().getId())) {
            setDatabaseColumnToNull(event.getGuild().getId(), "modlog");
        }

        if (transformer.getLevelChannel() != null && transformer.getLevelChannel().equals(event.getChannel().getId())) {
            setDatabaseColumnToNull(event.getGuild().getId(), "level_channel");
        }

        if (transformer.getMusicChannelText() != null && transformer.getMusicChannelText().equals(event.getChannel().getId())) {
            setDatabaseColumnToNull(event.getGuild().getId(), "music_channel_text");
        }
    }

    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());
        if (transformer == null) {
            return;
        }

        if (transformer.getMusicChannelVoice() != null && transformer.getMusicChannelVoice().equals(event.getChannel().getId())) {
            setDatabaseColumnToNull(event.getGuild().getId(), "music_channel_voice");
        }
    }

    public void updateChannelData(Guild guild) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", guild.getId())
                .update(statement -> {
                    statement.set("channels_data", GuildController.buildChannelData(guild.getTextChannels()), true);
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setDatabaseColumnToNull(String guildId, String column) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", guildId)
                .update(statement -> statement.set(column, null));
        } catch (SQLException ignored) {
            //
        }
    }
}
