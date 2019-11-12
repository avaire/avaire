package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.time.Carbon;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.SQLException;
import java.util.Base64;

public class SendRemindersTask implements Task {
    /**
     * Handles the task when the task is ready to be invoked.
     *
     * @param avaire The AvaIre class instance.
     */
    @Override
    public void handle(AvaIre avaire) {
        try
        {
            if (!avaire.getDatabase().getSchema().hasTable(Constants.REMINDERS_TABLE_NAME))
            {
                return;
            }

            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.REMINDERS_TABLE_NAME)
                                        .useAsync(true)
                                        .where("sent", false)
                                        .andWhere("expires_at", "<=", Carbon.now().addMinute())
                                        .get();

            if (collection.isEmpty())
            {
                return;
            }

            for (DataRow datarow :
                collection)
            {
                int id = datarow.getInt("id");
                User user = avaire.getShardManager().getUserById(datarow.getString("user_id"));
                TextChannel textChannel = avaire.getShardManager().getTextChannelById(datarow.getString("channel_id"));
                String content = new String(Base64.getDecoder().decode(datarow.getString("message")));
                Carbon timeStamp = datarow.getTimestamp("stored_at");
                if (textChannel == null || !textChannel.canTalk())
                {
                    sendPrivateMessage(avaire, user, content, timeStamp, id);
                }
                else
                {
                    textChannel.sendMessage(
                        buildMessage(user, content, timeStamp)
                    ).queue(
                        success -> markMessageSent(avaire, id),
                        error -> sendPrivateMessage(avaire, user, content, timeStamp, id));
                }
            }

            avaire.getDatabase().newQueryBuilder(Constants.REMINDERS_TABLE_NAME)
                .useAsync(true)
                .where("sent", true)
                .delete();

        } catch (SQLException ignored)
        {
            ignored.printStackTrace();
        }
    }

    private void sendPrivateMessage(AvaIre avaire, User user, String content, Carbon timeStamp, int id) {
        user.openPrivateChannel().queue(message -> message.sendMessage(
            buildMessage(user, content, timeStamp)).queue(success ->
            {
                markMessageSent(avaire, id);
            }
        ));
    }

    private Message buildMessage(User author, String content, Carbon timeStamp) {
        return new MessageBuilder()
                   .setContent(String.format("%s, %s you asked to be reminded about:",
                       author.getAsMention(),
                       timeStamp.diffForHumans()
                   ))
                   .setEmbed(new EmbedBuilder()
                                 .setDescription(content)
                                 .build()
                   ).build();
    }

    private void markMessageSent(AvaIre avaire, int id) {
        try
        {
            avaire.getDatabase().newQueryBuilder(Constants.REMINDERS_TABLE_NAME)
                .useAsync(true)
                .where("id", id)
                .update(statement -> statement.set("sent", true));
        } catch (SQLException ignored)
        {
            ignored.printStackTrace();
        }

    }
}
