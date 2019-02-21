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
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemindCommand extends Command {

    public RemindCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Remind Command";
    }

    @Override
    public String getDescription() {
        return "Reminds you of something after a certain amount of time.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command me <time> <message>`\n- Reminds you about the message after the time is up in a DM.",
            "`:command here <time> <message>`\n- Reminds you about the message after the time is up in the channel the command was used in."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command me 25m Something` - Reminds you about something after 25 minutes.",
            "`:command me 2h30m9s Stuff` - Reminds you about stuff after 2 hours, 30 minutes, and 9 seconds.",
            "`:command here 30m Potatoe` - Reminds you about Potatoe in 30 minutes in the current channel."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("remindme", "remind");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,60");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "type");
        }

        if (!(args[0].equalsIgnoreCase("here") || args[0].equalsIgnoreCase("me"))) {
            return sendErrorMessage(context, context.i18n("errors.invalidMeHere"));
        }
        boolean respondInDM = args[0].equalsIgnoreCase("me");

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "time");
        }

        final int time = parse(args[1]);
        if (time == 0) {
            return sendErrorMessage(context, "errors.invalidProperty", "time", "time format");
        }

        // Time must be greater than 60 seconds and less than 3 days.
        if (time < 60 || time > 259200) {
            return sendErrorMessage(context, context.i18n("errors.invalidTime"));
        }

        if (args.length == 2) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        handleReminderMessage(
            context,
            new MessageBuilder()
                .setContent(String.format("%s, %s you asked to be reminded about:",
                    context.getAuthor().getAsMention(),
                    Carbon.now().subSeconds(time).diffForHumans()
                ))
                .setEmbed(new EmbedBuilder()
                    .setDescription(String.join(" ", Arrays.copyOfRange(args, 2, args.length)))
                    .build()
                ).build(),
            time,
            respondInDM);

        context.makeInfo("Alright :user, in :time I'll remind you about :message")
            .set("time", Carbon.now().subSeconds(time).diffForHumans(true))
            .set("message", String.join(" ", Arrays.copyOfRange(args, 2, args.length)))
            .queue();

        return true;
    }

    private void handleReminderMessage(CommandMessage context, Message message, int time, boolean respondInDM) {
        if (respondInDM) {
            context.getAuthor().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage(message).queueAfter(time, TimeUnit.SECONDS, null, RestActionUtil.ignore);
            }, throwable -> {
                context.getMessageChannel().sendMessage(message).queueAfter(time, TimeUnit.SECONDS, null, RestActionUtil.ignore);
            });
        } else {
            context.getMessageChannel().sendMessage(message).queueAfter(time, TimeUnit.SECONDS, null, throwable -> {
                context.getAuthor().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(message).queueAfter(time, TimeUnit.SECONDS, null, RestActionUtil.ignore);
                });
            });
        }
    }

    public int parse(String input) {
        int result = 0;
        String number = "";
        String unit = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= '0' && c <= '9') {
                if (!unit.isEmpty() && !number.isEmpty()) {
                    result += convert(NumberUtil.parseInt(number, 0), unit);
                    number = "";
                    unit = "";
                }
                number += c;
            } else if (Character.isLetter(c) && !number.isEmpty()) {
                unit += c;
            }
        }
        if (!unit.isEmpty() && !number.isEmpty()) {
            result += convert(NumberUtil.parseInt(number, 0), unit);
        }
        return result;
    }

    private int convert(int value, String unit) {
        switch (unit.substring(0, 1).toLowerCase()) {
            case "d":
                return value * 60 * 60 * 24;
            case "h":
                return value * 60 * 60;
            case "m":
                return value * 60;
            case "s":
                return value;
        }
        return 0;
    }
}
