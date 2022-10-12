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
import com.avairebot.audio.*;
import com.avairebot.audio.exceptions.InvalidSearchProviderException;
import com.avairebot.audio.exceptions.SearchingException;
import com.avairebot.audio.exceptions.TrackLoadFailedException;
import com.avairebot.audio.searcher.SearchTrackResultHandler;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.exceptions.NoMatchFoundException;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PlayCommand extends Command {

    public PlayCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Play Music Command";
    }

    @Override
    public String getDescription() {
        return "Plays the provided song for you, if just the song title is given the bot will search YouTube for your song and give you some suggestions, you can also use YouTube, SoundCloud, TwitchTV, Bandcamp, and Vimeo link, or raw sound file, mp3, flac, wav, webm, mp4, ogg, aac, m3u and pls formats.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <song>` - Plays the given song");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(SoundcloudCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("play", "request");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:none",
            "throttle:guild,2,4",
            "musicChannel"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command A cool song` - Finds songs with the name \"A cool song\".",
            "`:command https://www.youtube.com/watch?v=dQw4w9WgXcQ` - Plays the song off a link"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_START_PLAYING);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingMusicQueue");
        }
    
        boolean shouldLeaveMessage = false;
        boolean quickPlay          = false;
    
        ArrayList<String> tempNewArgs = new ArrayList<>();
    
        for (String arg : args) {
            if (arg.equals("---leave-message")) {
                shouldLeaveMessage = true;
                continue;
            }
            if (arg.equals("---quick-play")) {
                quickPlay = true;
                continue;
            }
            tempNewArgs.add(arg);
        }
        args = tempNewArgs.toArray(new String[0]);
    
        if (quickPlay) {
            System.out.println("Performing Quick Play");
            return quickPlay(context, args, musicFailure(context));
        }
    
    
        VoiceConnectStatus voiceConnectStatus = AudioHandler.getDefaultAudioHandler().connectToVoiceChannel(context);
        if (!voiceConnectStatus.isSuccess()) {
            context.makeWarning(voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        if (AudioHandler.getDefaultAudioHandler().hasAudioSession(context) && NumberUtil.isNumeric(args[0])) {
            return loadSongFromSession(context, args);
        }

        AudioHandler.getDefaultAudioHandler().loadAndPlay(context, AudioHandler.getDefaultAudioHandler().createTrackRequestContext(context.getAuthor(), args)).handle(
            musicSuccess(context, shouldLeaveMessage),
            musicFailure(context),
            musicSession(context, args)
        );

        return true;
    }

    boolean loadSongFromSession(CommandMessage context, String[] args) {
        int songIndex = NumberUtil.parseInt(args[0], 1) - 1;
        AudioSession session = AudioHandler.getDefaultAudioHandler().getAudioSession(context);

        int index = NumberUtil.getBetween(songIndex, 0, session.getSongs().getTracks().size() - 1);
        AudioTrack track = session.getSongs().getTracks().get(index);

        Metrics.tracksLoaded.inc();

        musicSuccess(context, false).accept(
            new TrackResponse(AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild()),
                track,
                new TrackRequestContext(track)
            )
        );
        AudioHandler.getDefaultAudioHandler().play(context, AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild()), track);

        if (session.getMessage() != null) {
            session.getMessage().delete().queue(null, RestActionUtil.ignore);
        }

        AudioHandler.getDefaultAudioHandler().removeAudioSession(context);
        return false;
    }

    private void sendPlaylistResponse(CommandMessage context, TrackResponse response) {
        AudioPlaylist playlist = (AudioPlaylist) response.getAudioItem();

        context.makeSuccess(context.i18n("addedSongsFromPlaylist"))
            .set("songs", NumberUtil.formatNicely(playlist.getTracks().size()))
            .set("title", playlist.getName())
            .set("url", response.getTrackContext())
            .set("queueSize", NumberUtil.formatNicely(
                AudioHandler.getDefaultAudioHandler().getQueueSize(response.getMusicManager())
            ))
            .queue(message -> {
                if (context.getGuildTransformer() != null && context.getGuildTransformer().isMusicMessages()) {
                    message.delete().queueAfter(30, TimeUnit.SECONDS, null, RestActionUtil.ignore);
                }
            });
    }

    private void sendTrackResponse(CommandMessage context, TrackResponse response) {
        AudioTrack track = (AudioTrack) response.getAudioItem();

        context.makeSuccess(context.i18n("addedSong"))
            .set("title", track.getInfo().title)
            .set("url", track.getInfo().uri)
            .set("queueSize", NumberUtil.formatNicely(
                AudioHandler.getDefaultAudioHandler().getQueueSize(response.getMusicManager())
            ))
            .queue(message -> {
                if (context.getGuildTransformer() != null && context.getGuildTransformer().isMusicMessages()) {
                    message.delete().queueAfter(30, TimeUnit.SECONDS, null, RestActionUtil.ignore);
                }
            });
    }

    private Consumer<TrackResponse> musicSuccess(final CommandMessage context, final boolean finalShouldLeaveMessage) {
        return (TrackResponse response) -> {
            if (!finalShouldLeaveMessage && canDeleteMessage(context)) {
                context.delete().reason("Song request, removing song to cleanup chat").queue(null, RestActionUtil.ignore);
            }

            response.getMusicManager().registerDefaultVolume();

            if (response.getMusicManager().getPlayer().isPaused()) {
                response.getMusicManager().getPlayer().setPaused(false);
            }

            if (response.getMusicManager().getPlayer().getPlayingTrack() != null) {
                if (response.isPlaylist()) sendPlaylistResponse(context, response);
                else sendTrackResponse(context, response);
            }
        };
    }

    private boolean canDeleteMessage(CommandMessage context) {
        return context.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)
            && context.getGuild().getSelfMember().hasPermission(context.getChannel(), Permission.MESSAGE_MANAGE);
    }

    private Consumer<Throwable> musicFailure(final CommandMessage context) {
        return throwable -> context.makeError(throwable.getMessage()).queue();
    }
    
    private boolean quickPlay(final CommandMessage context, final String[] args, final Consumer<Throwable> failure) {
        TrackRequestContext trackContext = AudioHandler.getDefaultAudioHandler().createTrackRequestContext(context.getAuthor(), args);
        try {
            System.out.println("Quickplay Variables");
            AudioPlaylist     playlist          = new SearchTrackResultHandler(trackContext).searchSync();
            GuildMusicManager guildMusicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());
            System.out.println("Checking for music");
            if (playlist.getTracks() == null || playlist.getTracks().isEmpty() || playlist.getTracks().size() == 0) {
                failure.accept(new NoMatchFoundException(
                    context.i18nRaw("music.internal.noMatchFound", trackContext.getQuery()), trackContext));
            } else {
                 AudioHandler.getDefaultAudioHandler().createAudioSession(context, playlist);
                return loadSongFromSession(context, new String[]{"1"});
            }
        } catch (InvalidSearchProviderException | TrackLoadFailedException e) {
            failure.accept(new FriendlyException(
                context.i18nRaw("music.internal.trackLoadFailed", e.getMessage()),
                FriendlyException.Severity.COMMON,
                e
            ));
        } catch (SearchingException e) {
            failure.accept(new NoMatchFoundException(
                context.i18nRaw("music.internal.noMatchFound", trackContext.getQuery()),
                trackContext
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @SuppressWarnings("ConstantConditions")
    private Consumer<AudioSession> musicSession(final CommandMessage context, final String[] args) {
        return (AudioSession audioSession) -> {
            List<String> songs = new ArrayList<>();
            List<AudioTrack> tracks = audioSession.getSongs().getTracks();
            for (int i = 0; i < 9; i++) {
                if (tracks.size() <= i) {
                    break;
                }

                AudioTrack track = tracks.get(i);

                songs.add(String.format("`%s` [%s](%s) [%s]",
                    (i + 1), track.getInfo().title, track.getInfo().uri, NumberUtil.formatTime(track.getDuration())
                ));
            }

            String command = generateCommandTrigger(context.getMessage());
            if (args[0].startsWith("scsearch:")) {
                command = CommandHandler.getCommand(SoundcloudCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage());
            }

            context.makeSuccess(String.join("\n", songs))
                .setTitle(context.i18n("session.title", String.join(" ", args)))
                .setFooter(context.i18n("session.footer", command))
                .queue(audioSession::setMessage);
        };
    }
}
