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

package com.avairebot.audio;

import com.avairebot.AvaIre;
import com.avairebot.audio.source.PlaylistImportSourceManager;
import com.avairebot.utilities.NumberUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.*;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AudioPlayerManagerConfiguration implements Supplier<AudioPlayerManager> {

    private static final Logger log = LoggerFactory.getLogger(AudioPlayerManagerConfiguration.class);

    private final AvaIre avaire;
    private final AudioPlayerManager audioPlayerManager;

    /**
     * Creates a new audio player manager configuration instance
     * with a default audio player manager.
     *
     * @param avaire The main AvaIre application instance.
     */
    public AudioPlayerManagerConfiguration(AvaIre avaire) {
        this(avaire, new DefaultAudioPlayerManager());
    }

    /**
     * Creates a new audio player manager configuration instance
     * using the given audio player manager instance.
     *
     * @param avaire             The main AvaIre application instance.
     * @param audioPlayerManager The audio player manager that should be configured.
     */
    public AudioPlayerManagerConfiguration(AvaIre avaire, AudioPlayerManager audioPlayerManager) {
        this.avaire = avaire;
        this.audioPlayerManager = audioPlayerManager;

        log.debug("Creating new audio player manager instance");

        this.registerAudioSources(
            avaire.getConfig().getBoolean("audio-ratelimit.ip-blocks", false)
                ? buildRoutePlanner()
                : null
        );
    }

    @Override
    public AudioPlayerManager get() {
        return audioPlayerManager;
    }

    /**
     * Registers the audio sources that the audio player manager should support.
     *
     * @param routePlanner The route planner that should be used for YouTube
     *                     related music requests, or {@code NULL}
     */
    private void registerAudioSources(AbstractRoutePlanner routePlanner) {
        audioPlayerManager.registerSourceManager(new PlaylistImportSourceManager());

        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager();

        if (routePlanner != null) {
            new YoutubeIpRotatorSetup(routePlanner)
                .forSource(youtubeAudioSourceManager)
                .setup();
        } else {
            youtubeAudioSourceManager.configureRequests(config -> RequestConfig.copy(config)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build());
        }

        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
    }

    /**
     * Builds the router planner that should be used for YouTube related requests.
     *
     * @return The selected router planner strategy instance, or {@code NULL}
     * if no valid IP subnets have been provided, or an invalid
     * strategy was given.
     */
    private AbstractRoutePlanner buildRoutePlanner() {
        List<IpBlock> ipBlocks = new ArrayList<>();

        for (String cidrBlock : avaire.getConfig().getStringList("audio-ratelimit.ip-blocks")) {
            if (Ipv4Block.isIpv4CidrBlock(cidrBlock)) {
                ipBlocks.add(new Ipv4Block(cidrBlock));
            } else if (Ipv6Block.isIpv6CidrBlock(cidrBlock)) {
                ipBlocks.add(new Ipv6Block(cidrBlock));
            } else {
                log.warn("Invalid IP Block given! \"{}\" does not match any valid IPv4 or IPv6 CIDR Block", cidrBlock);
            }
        }

        if (ipBlocks.isEmpty()) {
            log.debug("List of ip blocks is empty, skipping setup of route planner");
            return null;
        }

        // Creates the predicate IP filter for IP addresses within the selected
        // subnets that should not be used, this completely excludes the IPs
        // from being used for audio requests.
        List<InetAddress> excludedAddresses = getExcludedAddresses();
        Predicate<InetAddress> filter = inetAddress -> {
            return !excludedAddresses.contains(inetAddress);
        };

        BigInteger totalAddresses = BigInteger.ZERO;
        for (IpBlock block : ipBlocks) {
            totalAddresses = totalAddresses.add(block.getSize());
        }
        log.debug("Attempting to register {} route planner with {} addresses.",
            getRatelimitStrategy(),
            NumberUtil.formatNicely(totalAddresses)
        );

        boolean searchTriggerFail = avaire.getConfig().getBoolean("audio-ratelimit.search-triggers-fail", true);
        switch (getRatelimitStrategy().toLowerCase()) {
            case "rotateonban":
                return new RotatingIpRoutePlanner(ipBlocks, filter, searchTriggerFail);

            case "loadbalance":
                return new BalancingIpRoutePlanner(ipBlocks, filter, searchTriggerFail);

            case "nanoswitch":
                return new NanoIpRoutePlanner(ipBlocks, searchTriggerFail);

            case "rotatingnanoswitch":
                return new RotatingNanoIpRoutePlanner(ipBlocks, filter, searchTriggerFail);

            default:
                throw new RuntimeException("Unknown audio ratelimit strategy!");
        }
    }

    /**
     * Gets the excluded IP addresses that should ignored from
     * the subnets when getting an IP for a request.
     *
     * @return The list of IP addresses to ignore/exclude for searches.
     */
    private List<InetAddress> getExcludedAddresses() {
        List<InetAddress> excludedAddresses = new ArrayList<>();

        for (String address : avaire.getConfig().getStringList("audio-ratelimit.exclude-ips")) {
            try {
                excludedAddresses.add(InetAddress.getByName(address));
            } catch (UnknownHostException e) {
                //
            }
        }

        return excludedAddresses;
    }

    /**
     * Gets the rate limit strategy that should be used as the router planner.
     *
     * @return The selected rate limit strategy.
     */
    private String getRatelimitStrategy() {
        return avaire.getConfig().getString("audio-ratelimit.strategy", "unknown").trim();
    }
}
