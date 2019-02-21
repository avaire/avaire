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
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.RandomUtil;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VoiceFixCommand extends Command {

    public VoiceFixCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Voice Fix Command";
    }

    @Override
    public String getDescription(@Nullable CommandContext context) {
        String prefix = context != null && context.isGuildMessage()
            ? generateCommandPrefix(context.getMessage())
            : DiscordConstants.DEFAULT_COMMAND_PREFIX;

        return String.format(String.join(" ",
            "Music will sometimes stop working when Discord forgets to notify bots",
            "about voice state changes, this commands tries to make fixing that a bit easier",
            "to do by forcing a voice update state for the bot through changing the server",
            "region, the command will pick a server region at random, swap the servers",
            "region to that, and then 2½ seconds later swap right back, this should",
            "fix music 99%s of the time.\n\n",
            "If you're still experiencing voice issues you can try making the bot leave the voice",
            "channel by using a command like `%sstop`, and then running this command again.\n\n",
            "Still having issues even after all that?\nYou can join the [support server](https://discord.gg/gt2FWER)",
            "to get help from the AvaIre support team directly."
        ), "%", prefix);
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Finds a random server region, swaps to it and then swaps back again.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("voicefix");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,1,60",
            "requireOne:user,general.manage_server,general.kick_members,general.ban_members",
            "require:bot,general.manage_server",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_SETTINGS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Region region = context.getGuild().getRegion();
        Region swapRegion = getRandomRegion(region);

        // Start the swapping process by sending the first status message and changing the server region to the swap region.
        context.getMessageChannel()
            .sendMessage(buildTodoEmbed(context, swapRegion, region, false, false))
            .queue(message -> context.getGuild().getManager().setRegion(swapRegion).queue(aVoid -> {
                    stageChangeToOriginalRegion(context, message, region, swapRegion);
                }, error -> logError(context, error)
            ), error -> logError(context, error));

        return true;
    }

    private void stageChangeToOriginalRegion(CommandMessage context, Message message, Region region, Region swapRegion) {
        message.editMessage(buildTodoEmbed(context, swapRegion, region, true, false))
            .queue(editMessage -> context.getGuild().getManager().setRegion(region).queueAfter(2500, TimeUnit.MILLISECONDS, bVoid -> {
                    stageChangeHasFinished(context, editMessage, region, swapRegion);
                }, error -> logError(context, error)
            ), error -> logError(context, error));
    }

    private void stageChangeHasFinished(CommandMessage context, Message message, Region region, Region swapRegion) {
        message.editMessage(buildTodoEmbed(context, swapRegion, region, true, true))
            .queue(null, error -> logError(context, error));
    }

    private MessageEmbed buildTodoEmbed(CommandMessage context, Region region, Region originalRegion, boolean firstSwap, boolean secondSwap) {
        return context.makeSuccess(buildMessage(context, firstSwap, secondSwap))
            .set("swap", region.getName())
            .set("original", originalRegion.getName())
            .buildEmbed();
    }

    private String buildMessage(CommandMessage context, boolean firstSwap, boolean secondSwap) {
        String message = context.i18n("message",
            firstSwap ? "☑" : "\uD83D\uDD18",
            secondSwap ? "☑" : "\uD83D\uDD18"
        );

        message = (firstSwap && secondSwap
            ? "~~" + context.i18n("starting") + "~~"
            : context.i18n("starting")
        ) + message;

        if (firstSwap && secondSwap) {
            message += context.i18n("finished");
        }

        return message;
    }

    private Region getRandomRegion(Region currentRegion) {
        List<Region> regions = new ArrayList<>();
        for (Region region : Region.values()) {
            if (region.equals(Region.UNKNOWN)
                || region.equals(currentRegion)
                || region.isVip() != currentRegion.isVip()) {
                continue;
            }
            regions.add(region);
        }
        return (Region) RandomUtil.pickRandom(regions);
    }

    private void logError(CommandMessage context, Throwable exception) {
        context.makeError(context.i18n("error", exception.getMessage()));
        AvaIre.getLogger().error("An error was thrown in the Voice Fix command while trying to change the server region: "
            + exception.getMessage(), exception
        );
    }
}
