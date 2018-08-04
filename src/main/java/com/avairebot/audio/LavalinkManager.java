package com.avairebot.audio;

import com.avairebot.AvaIre;
import com.avairebot.shared.DiscordConstants;
import lavalink.client.io.Lavalink;
import lavalink.client.io.metrics.LavalinkCollector;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class LavalinkManager {

    private Lavalink lavalink = null;
    private boolean enabled;

    public void start(AvaIre avaire) {
        enabled = avaire.getConfig().getBoolean("lavalink.enabled", false);

        if (!isEnabled()) {
            return;
        }

        List<Map<?, ?>> nodes = avaire.getConfig().getMapList("lavalink.nodes");
        if (nodes.isEmpty()) {
            enabled = false;
            return;
        }

        lavalink = new Lavalink(avaire.getConfig().getString("discord.clientId", "" + DiscordConstants.AVAIRE_BOT_ID),
            avaire.getSettings().getShardCount() < 1 ? 1 : avaire.getSettings().getShardCount(),
            shardId -> avaire.getShardManager().getShardById(shardId)
        );

        for (Map<?, ?> node : nodes) {
            if (!node.containsKey("name") || !node.containsKey("host") || !node.containsKey("pass")) {
                continue;
            }

            try {
                URI host = new URI((String) node.get("host"));

                lavalink.addNode(
                    (String) node.get("name"), host, (String) node.get("pass")
                );
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        new LavalinkCollector(lavalink).register();
    }

    public boolean isEnabled() {
        return enabled;
    }

    IPlayer createPlayer(String guildId) {
        return isEnabled()
            ? lavalink.getLink(guildId).getPlayer()
            : new LavaplayerPlayerWrapper(AudioHandler.getDefaultAudioHandler().getPlayerManager().createPlayer());
    }

    public void openConnection(VoiceChannel channel) {
        if (isEnabled()) {
            lavalink.getLink(channel.getGuild()).connect(channel);
        } else {
            channel.getGuild().getAudioManager().openAudioConnection(channel);
        }
    }

    public void closeConnection(Guild guild) {
        if (isEnabled()) {
            lavalink.getLink(guild).disconnect();
        } else {
            guild.getAudioManager().closeAudioConnection();
        }
    }

    public VoiceChannel getConnectedChannel(@Nonnull Guild guild) {
        return guild.getSelfMember().getVoiceState().getChannel();
    }

    public Lavalink getLavalink() {
        return lavalink;
    }

    public static class LavalinkManagerHolder {
        public static final LavalinkManager lavalink = new LavalinkManager();
    }
}
