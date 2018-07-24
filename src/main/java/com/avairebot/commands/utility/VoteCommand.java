package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.time.Carbon;
import com.avairebot.vote.VoteCacheEntity;
import com.avairebot.vote.VoteEntity;

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
    public List<String> getTriggers() {
        return Collections.singletonList("vote");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        VoteCacheEntity voteEntity = avaire.getVoteManager().getVoteEntity(avaire, context.getAuthor());

        if (args.length > 0 && args[0].equalsIgnoreCase("check")) {
            return checkUser(context, voteEntity);
        }

        String note = String.format(String.join("\n", Arrays.asList(
            "You'll gain access to the `!volume` and `!default-volume` commands for the",
            "next 24 hours, as well as getting a vote point, rewards for vote points is",
            "coming soon!",
            "",
            "Have you already voted and didn't get your vote rewards?",
            "Try run `%s check`"
        )), generateCommandTrigger(context.getMessage()));

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
