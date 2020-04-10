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
import com.avairebot.Constants;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.RemindersController;
import com.avairebot.database.transformers.RemindersTransformer;
import com.avairebot.database.transformers.RemindersTransformer.Reminder;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class RemindCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(RemindCommand.class);

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
            "`:command here <time> <message>`\n- Reminds you about the message after the time is up in the channel the command was used in.",
            "`:command list <page number>`\n - showing up to 10 at a time, with the message, where\n" + "the user will received the message(channel or DM), and when they will receive the message"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command me 25m Something` - Reminds you about something after 25 minutes.",
            "`:command me 2h30m9s Stuff` - Reminds you about stuff after 2 hours, 30 minutes, and 9 seconds.",
            "`:command here 30m Potato` - Reminds you about Potato in 30 minutes in the current channel.",
            "`:command list 2` - List all pending reminders on page 2"
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

        if(args[0].equalsIgnoreCase("list"))
        {
            return sendReminderList(context,args);
        }
        else if(args[0].equalsIgnoreCase("delete"))
        {
            return deleteReminder(context,args);
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
            String.join(" ", Arrays.copyOfRange(args, 2, args.length)),
            time,
            respondInDM);

        context.makeInfo("Alright :user, in :time I'll remind you about :message")
            .set("time", Carbon.now().subSeconds(time).diffForHumans(true))
            .set("message", String.join(" ", Arrays.copyOfRange(args, 2, args.length)))
            .queue();

        return true;
    }

    private boolean deleteReminder(CommandMessage context, String[] args)
    {
        int id = Integer.parseInt(args[1]);
        boolean reminderExists = false;
        RemindersTransformer reminders = RemindersController.fetchPendingReminders(context.getAuthor().getIdLong());
        for (Reminder reminder: reminders.getReminders())
        {
            if(reminder.getId() == id && reminder.getUserId() == context.getAuthor().getIdLong())
            {
                reminderExists = true;
                break ;
            }
        }
        if(!reminderExists)
        {
            return sendErrorMessage(context,"No reminder found with " + id);
        }
        try
        {
            avaire.getDatabase().newQueryBuilder(Constants.REMINDERS_TABLE_NAME)
                .where("id",id)
                .andWhere("user_id",context.getAuthor().getIdLong())
                .delete();
            RemindersController.cache.invalidate(context.getAuthor().getIdLong());
            return true;
        }
        catch (SQLException e)
        {
            log.debug(e.getMessage());
        }
        return true;
    }

    private boolean sendReminderList(CommandMessage context, String[] args)
    {
        RemindersTransformer transformer = RemindersController.fetchPendingReminders(context.getAuthor().getIdLong());
        List<Reminder> reminders = transformer.getReminders();
        ArrayList<String> reminderList = new ArrayList<String>();
        for (Reminder reminder: reminders)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("ID:").append( reminder.getId()).append("\n");
            builder.append("Message: ")
                   .append(reminder.getMessage())
                    .append("\n")
                   .append("Location:");
                    if(reminder.getChannelId() == null || reminder.getChannelId().isEmpty())
                    {
                        builder.append("DM");
                    }
                    else
                    {
                        builder.append(context.getGuild()
                                        .getTextChannelById(reminder.getChannelId())
                                        .getAsMention());
                    }
                   builder.append("\n");
                   builder.append("Remaining Time:")
                          .append(reminder.getExpirationDate().diffForHumans(true));
                   reminderList.add(builder.toString());
        }
        SimplePaginator<String> paginator = new SimplePaginator<String>(reminderList,10);
        if (args.length > 1) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
        }
        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add(val));

        context.makeInfo(":reminders\n\n:paginator")
            .setTitle(context.i18n("title"))
            .set("reminders",String.join("\n",messages))
            .set("paginator", paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())))
            .queue();
        return false;

    }

    private void handleReminderMessage(CommandMessage context, String message, int time, boolean respondInDM)
    {
        String encodedString =  Base64.getEncoder().encodeToString(message.getBytes());
        try
        {
            if (respondInDM)
            {
                avaire.getDatabase().newQueryBuilder(Constants.REMINDERS_TABLE_NAME)
                    .insert(statement -> {
                        statement.set("user_id", context.getAuthor().getIdLong());
                        statement.set("message", encodedString);
                        statement.set("channel_id", null);
                        statement.set("stored_at",Carbon.now());
                        statement.set("expires_at",Carbon.now().addSeconds(time));
                    });

            }
            else
            {
                avaire.getDatabase().newQueryBuilder(Constants.REMINDERS_TABLE_NAME)
                    .insert(statement -> {
                        statement.set("user_id", context.getAuthor().getIdLong());
                        statement.set("message", encodedString);
                        statement.set("channel_id", context.getMessageChannel().getId());
                        statement.set("stored_at",Carbon.now());
                        statement.set("expires_at", Carbon.now().addSeconds(time));
                    });
            }
        }
        catch(SQLException e)
        {
            log.error("Something went wrong while a use was trying to store a reminder: {}", e.getMessage(), e);

            sendErrorMessage(context, context.i18n("failedToStoreInfo", e.getMessage()));
        }
    }

    public int parse(String input) {
        int result = 0;
        StringBuilder number = new StringBuilder();
        StringBuilder unit = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= '0' && c <= '9') {
                if ((unit.length() > 0) && (number.length() > 0)) {
                    result += convert(NumberUtil.parseInt(number.toString(), 0), unit.toString());
                    number = new StringBuilder();
                    unit = new StringBuilder();
                }
                number.append(c);
            } else if (Character.isLetter(c) && (number.length() > 0)) {
                unit.append(c);
            }
        }
        if ((unit.length() > 0) && (number.length() > 0)) {
            result += convert(NumberUtil.parseInt(number.toString(), 0), unit.toString());
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
            default:
                return 0;
        }
    }
}
