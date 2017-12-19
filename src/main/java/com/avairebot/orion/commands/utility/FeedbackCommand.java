package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FeedbackCommand extends Command {

    public FeedbackCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Feedback Command";
    }

    @Override
    public String getDescription() {
        return "Gives feedback about Orion to the developers and staff team";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <message>` - Sends feedback to the devs.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command The thing about the stuff is doing stuff that doesn't make sense for the thing.`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("feedback");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,60");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument `message`, you must include a message.");
        }

        try {
            orion.getDatabase().newQueryBuilder(Constants.FEEDBACK_TABLE_NAME)
                .insert(statement -> {
                    statement.set("message", String.join(" ", args), true);
                    statement.set("user", buildJsonUser(message.getAuthor()), true);
                    statement.set("channel", buildJsonChannel(message.getTextChannel()), true);
                    statement.set("guild", buildJsonGuild(message.getGuild()), true);
                });

            MessageFactory.makeSuccess(message, "Successfully sent feedback <:tickYes:319985232306765825>").queue();
        } catch (SQLException e) {
            e.printStackTrace();

            MessageFactory.makeError(message, "An error occurred while attempting to send your feedback:\n**Error:** " + e.getMessage()).queue();
        }
        return true;
    }

    private String buildJsonUser(User user) {
        String username = EmojiParser.removeAllEmojis(user.getName());
        if (username.length() == 0) {
            username = "Invalid Username";
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", username);
        map.put("discriminator", user.getDiscriminator());
        map.put("avatar", user.getAvatarId());

        return Orion.GSON.toJson(map);
    }

    private String buildJsonChannel(TextChannel channel) {
        String name = EmojiParser.removeAllEmojis(channel.getName());
        if (name.length() == 0) {
            name = "Invalid Channel Name";
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("id", channel.getId());
        map.put("name", name);

        return Orion.GSON.toJson(map);
    }

    private String buildJsonGuild(Guild guild) {
        if (guild == null) {
            return null;
        }

        String name = EmojiParser.removeAllEmojis(guild.getName());
        if (name.length() == 0) {
            name = "Invalid Guild Name";
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("id", guild.getId());
        map.put("name", name);
        map.put("owner_id", guild.getOwner().getUser().getId());
        map.put("icon", guild.getIconId());

        return Orion.GSON.toJson(map);
    }
}
