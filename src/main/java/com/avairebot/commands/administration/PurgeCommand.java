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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.modlog.Modlog;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogType;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.MiscUtil;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PurgeCommand extends Command {

    public PurgeCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Purge Command";
    }

    @Override
    public String getDescription() {
        return "Deletes up to 100 chat messages in any channel, you can mention a user if you only want to delete messages by the mentioned user.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Deletes the last 5 messages.",
            "`:command [number]` - Deletes the given number of messages.",
            "`:command [number] [user]` - Deletes the given number of messages for the mentioned users."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 56`",
            "`:command 30 @Senither`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("purge", "clear");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,text.manage_messages",
            "require:bot,text.manage_messages,text.read_message_history",
            "throttle:channel,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MODERATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        int toDelete = 5;
        if (args.length > 0) {
            toDelete = NumberUtil.getBetween(NumberUtil.parseInt(args[0]), 1, 100);
        }

        int finalToDelete = toDelete;
        context.getMessage().delete().queue(ignored -> {
            if (args.length < 1) {
                handleCommand(context, new String[0], finalToDelete);
                return;
            }

            handleCommand(
                context, Arrays.copyOfRange(args, 1, args.length), finalToDelete
            );
        }, RestActionUtil.ignore);

        return true;
    }

    private void handleCommand(CommandMessage context, String[] args, final int toDelete) {
        Set<Long> userIds = new HashSet<>();
        for (int i = 0; i < args.length; i++) {
            User user = MentionableUtil.getUser(context, args, i);
            if (user != null) {
                userIds.add(user.getIdLong());
            }
        }

        if (userIds.isEmpty()) {
            context.getChannel().getHistoryBefore(context.getMessage(), 2).queue(history -> {
                loadMessages(context.getChannel().getHistory(), toDelete, null, messages -> {
                    if (messages.isEmpty()) {
                        sendNoMessagesMessage(context, null, toDelete);
                        return;
                    }

                    deleteMessages(context, messages).queue(aVoid -> {
                        Modlog.log(avaire, context, new ModlogAction(
                                ModlogType.PURGE,
                                context.getAuthor(), null,
                                context.i18n("messagesDeletedIn", messages.size(), context.getChannel().getAsMention())
                            )
                        );

                        context.makeSuccess(context.i18n("success"))
                            .set("number", messages.size())
                            .queue(successMessage -> successMessage.delete().queueAfter(8, TimeUnit.SECONDS, null, RestActionUtil.ignore));
                    }, RestActionUtil.ignore);
                });
            });
            return;
        }

        loadMessages(context.getChannel().getHistory(), toDelete, userIds, messages -> {
            if (messages.isEmpty()) {
                sendNoMessagesMessage(context, userIds, toDelete);
                return;
            }

            deleteMessages(context, messages).queue(aVoid -> {
                List<String> users = new ArrayList<>();
                for (Long userId : userIds) {
                    users.add(String.format("<@%s>", userId));
                }

                Modlog.log(avaire, context, new ModlogAction(
                        ModlogType.PURGE,
                        context.getAuthor(), null,
                        context.i18n("messagesDeletedBy",
                            messages.size(),
                            String.join(", ", users),
                            context.getChannel().getAsMention()
                        )
                    )
                );

                context.makeSuccess(context.i18n("successBy"))
                    .set("number", messages.size())
                    .set("users", String.join(", ", users))
                    .queue(successMessage -> successMessage.delete().queueAfter(8, TimeUnit.SECONDS, null, RestActionUtil.ignore));
            }, RestActionUtil.ignore);
        });
    }

    private void loadMessages(MessageHistory history, int toDelete, Set<Long> userIds, Consumer<List<Message>> consumer) {
        long maxMessageAge = (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14) - MiscUtil.DISCORD_EPOCH) << MiscUtil.TIMESTAMP_OFFSET;
        List<Message> messages = new ArrayList<>();

        history.retrievePast(toDelete).queue(historyMessages -> {
            if (historyMessages.isEmpty()) {
                consumer.accept(messages);
                return;
            }

            for (Message historyMessage : historyMessages) {
                if (historyMessage.isPinned() || historyMessage.getIdLong() < maxMessageAge) {
                    continue;
                }

                if (userIds != null && !userIds.contains(historyMessage.getAuthor().getIdLong())) {
                    continue;
                }

                if (messages.size() >= toDelete) {
                    consumer.accept(messages);
                    return;
                }

                messages.add(historyMessage);
            }

            consumer.accept(messages);
        });
    }

    private void sendNoMessagesMessage(CommandMessage context, Set<Long> userIds, int toDelete) {
        PlaceholderMessage message;

        if (userIds == null) {
            message = context.makeSuccess(context.i18n("nothingToDelete")
            );
        } else {
            List<String> users = new ArrayList<>();
            for (Long userId : userIds) {
                users.add(String.format("<@%s>", userId));
            }

            message = context.makeSuccess(context.i18n("nothingToDeleteBy"))
                .set("users", String.join(", ", users))
                .set("number", toDelete);
        }

        message.queue(successMessage -> successMessage.delete().queueAfter(8, TimeUnit.SECONDS, null, RestActionUtil.ignore));
    }

    private RestAction<Void> deleteMessages(CommandMessage context, List<Message> messages) {
        if (messages.size() == 1) {
            return messages.get(0).delete();
        }
        return context.getChannel().deleteMessages(messages);
    }
}
