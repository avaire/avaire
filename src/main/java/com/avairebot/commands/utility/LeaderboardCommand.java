package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.LevelUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "leaderboard-command")
public class LeaderboardCommand extends Command {

    public LeaderboardCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Leaderboard Command";
    }

    @Override
    public String getDescription() {
        return "Shows the top 100 users, their rank, level, and xp for the current server.";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("leaderboard", "top");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, message);
        if (transformer == null || !transformer.isLevels()) {
            return sendErrorMessage(message, "This command requires the `Levels & Experience` feature to be enabled for the server, you can ask a server admin if they want to enable it with `.level`");
        }

        Collection collection = loadTop100From(message);
        if (collection == null) {
            MessageFactory.makeWarning(message, "There are no leaderboard data right now, try again later.").queue();
            return false;
        }

        List<String> messages = new ArrayList<>();
        SimplePaginator paginator = new SimplePaginator(collection.getItems(), 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        paginator.forEach((index, key, val) -> {
            DataRow row = (DataRow) val;

            Member member = message.getGuild().getMemberById(row.getLong("user_id"));
            String username = row.getString("username") + "#" + row.getString("discriminator");
            if (member != null) {
                username = member.getUser().getName() + "#" + member.getUser().getDiscriminator();
            }

            long experience = row.getLong("experience", 100);

            messages.add(String.format("`%s` **%s** is level **%s** with **%s** xp.",
                index + 1,
                username,
                LevelUtil.getLevelFromExperience(experience),
                experience - 100
            ));
        });

        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(message)));

        MessageFactory.makeInfo(message, String.join("\n", messages))
            .setTitle(message.getGuild().getName() + " Leaderboard", "https://avairebot.com/leaderboard/" + message.getGuild().getId())
            .queue();

        return true;
    }

    private Collection loadTop100From(Message message) {
        return (Collection) avaire.getCache().getAdapter(CacheType.MEMORY).remember("database-xp-leaderboard." + message.getGuild().getId(), 60, () -> {
            return avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .where("guild_id", message.getGuild().getId())
                .orderBy("experience", "desc")
                .take(100)
                .get();
        });
    }
}
