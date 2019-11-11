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
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotator;
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

    public AudioPlayerManagerConfiguration(AvaIre avaire) {
        this(avaire, new DefaultAudioPlayerManager());
    }

    public AudioPlayerManagerConfiguration(AvaIre avaire, AudioPlayerManager audioPlayerManager) {
        this.avaire = avaire;
        this.audioPlayerManager = audioPlayerManager;

        log.debug("Creating new audio player manager instance");

        this.registerAudioSources(
            buildRoutePlanner()
        );
    }

    @Override
    public AudioPlayerManager get() {
        return audioPlayerManager;
    }

    private void registerAudioSources(AbstractRoutePlanner routePlanner) {
        audioPlayerManager.registerSourceManager(new PlaylistImportSourceManager());

        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager();
        youtubeAudioSourceManager.configureRequests(config -> RequestConfig.copy(config)
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build());

        if (routePlanner != null) {
            YoutubeIpRotator.setup(youtubeAudioSourceManager, routePlanner);
        }

        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        audioPlayerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
    }

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

    private String getRatelimitStrategy() {
        return avaire.getConfig().getString("audio-ratelimit.strategy").trim();
    }
}
