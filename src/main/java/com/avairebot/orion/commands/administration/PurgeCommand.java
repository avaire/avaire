package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PurgeCommand extends Command {

    private static final int MAX_HISTORY_LOOPS = 25;

    public PurgeCommand(Orion orion) {
        super(orion, false);
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

    @Override
    public boolean onCommand(Message message, String[] args) {
        int toDelete = 100;
        if (args.length > 0) {
            toDelete = NumberUtil.getBetween(NumberUtil.parseInt(args[0]), 1, 100);
        }

        message.getChannel().sendTyping().queue();
        if (message.getMentionedUsers().isEmpty()) {
            loadMessages(message.getChannel().getHistory(), toDelete, new ArrayList<>(), null, 1, messages -> {
                if (messages.isEmpty()) {
                    sendNoMessagesMessage(message);
                    return;
                }

                deleteMessages(message, messages).queue(aVoid ->
                    MessageFactory.makeSuccess(message, ":white_check_mark: `:number` messages has been deleted!")
                        .set("number", messages.size())
                        .queue(successMessage -> successMessage.delete().queueAfter(8, TimeUnit.SECONDS)));
            });
            return true;
        }

        List<Long> userIds = new ArrayList<>();
        for (User user : message.getMentionedUsers()) {
            userIds.add(user.getIdLong());
        }

        loadMessages(message.getChannel().getHistory(), toDelete, new ArrayList<>(), userIds, 1, messages -> {
            if (messages.isEmpty()) {
                sendNoMessagesMessage(message);
                return;
            }

            deleteMessages(message, messages).queue(aVoid -> {
                List<String> users = new ArrayList<>();
                for (Long userId : userIds) {
                    users.add(String.format("<@%s>", userId));
                }

                MessageFactory.makeSuccess(message, ":white_check_mark: `:number` messages has been deleted from :users")
                    .set("number", messages.size())
                    .set("users", String.join(", ", users))
                    .queue(successMessage -> successMessage.delete().queueAfter(8, TimeUnit.SECONDS));
            });
        });
        return true;
    }

    private void loadMessages(MessageHistory history, int toDelete, List<Message> messages, List<Long> userIds, int loops, Consumer<List<Message>> consumer) {
        long maxMessageAge = (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14) - MiscUtil.DISCORD_EPOCH) << MiscUtil.TIMESTAMP_OFFSET;

        history.retrievePast(100).queue(historyMessages -> {
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

                if (messages.size() >= toDelete || loops > MAX_HISTORY_LOOPS) {
                    consumer.accept(messages);
                    return;
                }

                messages.add(historyMessage);
            }

            loadMessages(history, toDelete, messages, userIds, loops + 1, consumer);
        });
    }

    private void sendNoMessagesMessage(Message message) {
        MessageFactory.makeSuccess(message,
            ":x: Nothing to delete, I am unable to delete messages older than 14 days."
        ).queue(successMessage -> successMessage.delete().queueAfter(8, TimeUnit.SECONDS));
    }

    private RestAction<Void> deleteMessages(Message message, List<Message> messages) {
        if (messages.size() == 1) {
            return messages.get(0).delete();
        }
        return message.getTextChannel().deleteMessages(messages);
    }
}
