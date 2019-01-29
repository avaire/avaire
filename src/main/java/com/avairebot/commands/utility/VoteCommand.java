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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlayCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.language.I18n;
import com.avairebot.time.Carbon;
import com.avairebot.vote.VoteCacheEntity;
import com.avairebot.vote.VoteEntity;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VoteCommand extends Command {

    public VoteCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Vote Command";
    }

    @Override
    public String getDescription() {
        return "Enjoy using the bot? Consider voting for the bot to help it grow, it's free but means a lot to the team behind Ava <3";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command check` - Checks if you have voted for Ava in the last 12 hours.",
            "`:command` - Displays the invite link to Ava, or tells you when your vote expires."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command check`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("vote");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.BOT_INFORMATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        VoteCacheEntity voteEntity = avaire.getVoteManager().getVoteEntity(context.getAuthor());

        if (args.length > 0 && args[0].equalsIgnoreCase("check")) {
            return checkUser(context, voteEntity);
        }

        String utilityPrefix = generateCommandPrefix(context.getMessage());

        //noinspection ConstantConditions
        String note = I18n.format(String.join("\n", Arrays.asList(
            "You'll gain access to the `{0}volume` and `{0}default-volume` commands for the",
            "next 12 hours, as well as getting a vote point, you can spend your vote points",
            "to unlock special rank backgrounds using the `{1}backgrounds` command.",
            "",
            "Have you already voted and didn't get your vote rewards?",
            "Try run `{1}vote check`"
        )), CommandHandler.getCommand(PlayCommand.class).getCategory().getPrefix(context.getMessage()), utilityPrefix);

        Carbon expire = avaire.getVoteManager().getExpireTime(context.getAuthor());
        if (expire != null && expire.isFuture()) {
            note = "You have already voted today, thanks for that btw!\nYou can vote again in " + expire.diffForHumans() + ".";
        }

        context.makeSuccess(String.join("\n", Arrays.asList(
            "Enjoy using the bot? Consider voting for the bot to help it grow, it's free but means a lot to the team behind Ava ❤",
            "",
            "https://discordbots.org/bot/avaire",
            "",
            ":note"
        )))
            .set("note", note)
            .setTitle("Vote for AvaIre on DBL", "https://discordbots.org/bot/avaire")
            .setFooter("You have " + (voteEntity == null ? 0 : voteEntity.getVotePoints()) + " vote points")
            .queue();

        return true;
    }

    private boolean checkUser(CommandMessage context, VoteCacheEntity voteEntity) {
        Carbon expire = avaire.getVoteManager().getExpireTime(context.getAuthor());
        if (expire != null && expire.isFuture()) {
            context.makeInfo("You have already voted today, thanks for that btw!\nYou can vote again in :time.")
                .setFooter("You have " + (voteEntity == null ? 0 : voteEntity.getVotePoints()) + " vote points")
                .set("time", expire.diffForHumans())
                .queue();
            return true;
        }

        boolean wasAdded = avaire.getVoteManager().queueEntity(new VoteEntity(
            context.getAuthor().getIdLong(),
            context.getMessageChannel().getIdLong()
        ));

        if (!wasAdded) {
            return false;
        }

        context.makeInfo(
            ":user, You've been put on a queue to get your vote rewards. Make sure you've voted or nothing will happen!"
        ).queue();

        return true;
    }
}
