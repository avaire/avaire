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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.imagegen.RankBackgrounds;
import com.avairebot.language.I18n;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.vote.VoteCacheEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RankBackgroundCommand extends Command {

    public RankBackgroundCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Rank Background Command";
    }

    @Override
    public String getDescription() {
        return "TODO";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "TODO"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(":command");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(
            RankCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("backgrounds", "rankbg", "levelbg");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "option");
        }

        switch (args[0].toLowerCase().trim()) {
            case "list":
                return handleList(context, prepareArguments(args));

            case "show":
            case "test":
                return handleShow(context, prepareArguments(args));

            case "buy":
            case "purchases":
                return handlePurchases(context, prepareArguments(args));

            default:
                return sendErrorMessage(context, "errors.invalidProperty", "option", "option");
        }
    }

    private boolean handleList(CommandMessage context, String[] args) {
        PlayerTransformer player = context.getPlayerTransformer();
        if (player == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "player transformer");
        }

        VoteCacheEntity voteEntity = avaire.getVoteManager().getVoteEntity(context.getAuthor());
        int votePoints = voteEntity == null ? 0 : voteEntity.getVotePoints();

        SimplePaginator paginator = new SimplePaginator(
            RankBackgrounds.getNameToCost(), 5, 1
        );

        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> message = new ArrayList<>();
        message.add(I18n.format(
            "Rank backgrounds can be unlocked using **[Vote Points]({0})**,",
            "https://discordbots.org/bot/avaire"
        ));
        message.add("you'll get **1 point** each time you vote for the bot.");
        message.add("-------------------------------");

        String purchaseType = RankBackgrounds.getDefaultBackground().getPurchaseType();
        paginator.forEach((index, name, cost) -> {
            //noinspection ConstantConditions
            boolean alreadyOwns = player.hasPurchases() && player.getPurchases().hasPuraches(
                purchaseType, RankBackgrounds.fromName((String) name).getId()
            );

            if (alreadyOwns) {
                message.add(I18n.format("**{0}**\n - _You already own this background._", name));
            } else {
                message.add(I18n.format("**{0}**\n - Costs {1} vote points", name, cost));
            }
        });

        message.add("-------------------------------");
        message.add(paginator.generateFooter(generateCommandTrigger(context.getMessage()) + " "));

        context.makeInfo(String.join("\n", message))
            .setTitle(I18n.format("Rank Backgrounds ({0})", paginator.getTotal()))
            .setFooter("You have " + votePoints + " vote points")
            .queue();

        return false;
    }

    private boolean handleShow(CommandMessage context, String[] args) {
        return false;
    }

    private boolean handlePurchases(CommandMessage context, String[] args) {
        return false;
    }

    private String[] prepareArguments(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
