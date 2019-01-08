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

package com.avairebot.contracts.handlers;

import com.avairebot.handlers.events.ApplicationShutdownEvent;
import com.avairebot.handlers.events.ModlogActionEvent;
import com.avairebot.handlers.events.MusicEndedEvent;
import com.avairebot.handlers.events.NowPlayingEvent;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class EventListener extends ListenerAdapter implements net.dv8tion.jda.core.hooks.EventListener {

    /**
     * The modlog actionable event will be called when a modlog action is
     * carried out on a server with the modlog feature enabled.
     *
     * @param event The modlog actionable event.
     */
    public void onModlogAction(ModlogActionEvent event) {
        //
    }

    /**
     * The application shutdown event, this event will be called just before
     * the application shuts down, the event will be invoked when a bot
     * administrator uses a shutdown, restart, or update command.
     *
     * @param event The application shutdown event.
     */
    public void onApplicationShutdown(ApplicationShutdownEvent event) {
        //
    }

    /**
     * The music now playing event, this event will be called whenever
     * the bot starts playing a new song, the guild the song is
     * being played in can be found through th event.
     *
     * @param event The music now playing event.
     */
    public void onNowPlaying(NowPlayingEvent event) {
        //
    }

    /**
     * The end of the music event, this event will be called just before
     * the bot leaves the voice channel when the music has ended, this
     * can be due to one of the following things.
     * <p>
     * <ol>
     * <li>The queue is empty and the song that was playing has ended.</li>
     * <li>A user used the !stop or !skip command with no other songs in the queue.</li>
     * <li>No one was listening to the music and the music activity job stopped the music.</li>
     * </ol>
     *
     * @param event The end of the music event.
     */
    public void onMusicEnded(MusicEndedEvent event) {
        //
    }

    /**
     * Handles the given custom event by passing the event to
     * the correct method, and calls the generic event
     * handler to log the event to the metrics.
     *
     * @param event The custom event that should be handled.
     */
    public final void onCustomEvent(Event event) {
        onGenericEvent(event);

        if (event instanceof ModlogActionEvent) {
            onModlogAction((ModlogActionEvent) event);
        } else if (event instanceof ApplicationShutdownEvent) {
            onApplicationShutdown((ApplicationShutdownEvent) event);
        } else if (event instanceof NowPlayingEvent) {
            onNowPlaying((NowPlayingEvent) event);
        } else if (event instanceof MusicEndedEvent) {
            onMusicEnded((MusicEndedEvent) event);
        }
    }
}
