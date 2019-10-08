/*
 * Copyright (c) 2019.
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

package com.avairebot.audio.cache;

import com.avairebot.audio.AudioHandler;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class AudioTrackSerializer {

    /**
     * Encodes the given audio track into a byte array which an be
     * used to re-create the original audio track later.
     *
     * @param audioTrack The LavaPlayer AudioTrack object instance.
     * @return The encoded byte value of the given audio track, or {@code null} if something went wrong.
     */
    @Nullable
    public static byte[] encodeTrack(AudioTrack audioTrack) {
        if (audioTrack == null) {
            return null;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            AudioHandler.getDefaultAudioHandler().getPlayerManager()
                .encodeTrack(new MessageOutput(baos), audioTrack);

            return baos.toByteArray();
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Decodes the given byte array into a LavaPlayer AudioTrack
     * instance, creating the original audio track object.
     *
     * @param input The byte array which should be decoded into the AudioTrack instance.
     * @return The decoded LavaPlayer AudioTrack object instance, or {@code null} if
     *         the given byte array does not match a audio track.
     */
    @Nullable
    public static AudioTrack decodeTrack(byte[] input) {
        if (input == null) {
            return null;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
            return AudioHandler.getDefaultAudioHandler().getPlayerManager()
                .decodeTrack(new MessageInput(bais)).decodedTrack;
        } catch (IOException e) {
            return null;
        }
    }
}
