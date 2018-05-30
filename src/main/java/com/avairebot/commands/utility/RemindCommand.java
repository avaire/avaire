package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;

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
        return Collections.singletonList("`:command <time> <message>` - Reminds you about the message after the time is up.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 25m Something` - Reminds you about something after 25 minutes.",
            "`:command 2h30m9s Stuff` - Reminds you about stuff after 2 hours, 30 minutes, and 9 seconds."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("remind");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,60");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "time");
        }

        final int time = parse(args[0]);
        if (time == 0) {
            return sendErrorMessage(context, "errors.invalidProperty", "time", "time format");
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        String message = String.format("%s, %s you asked to be reminded about %s",
            context.getAuthor().getAsMention(),
            Carbon.now().subSeconds(time).diffForHumans(),
            String.join(" ", Arrays.copyOfRange(args, 1, args.length))
        );

        context.getAuthor().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage(message).queueAfter(time, TimeUnit.SECONDS, null, RestActionUtil.IGNORE);
        }, throwable -> {
            context.getMessageChannel().sendMessage(message).queueAfter(time, TimeUnit.SECONDS, null, RestActionUtil.IGNORE);
        });

        context.makeInfo("Alright :user, in :time I'll remind you about :message")
            .set("time", Carbon.now().subSeconds(time).diffForHumans(true))
            .set("message", String.join(" ", Arrays.copyOfRange(args, 1, args.length)))
            .queue();

        return true;
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
