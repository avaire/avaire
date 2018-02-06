package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.administration.LevelCommand;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.LevelUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;

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
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, context.getMessage());
        if (transformer == null || !transformer.isLevels()) {
            return sendErrorMessage(context,
                "This command requires the `Levels & Experience` feature to be enabled for the server, you can ask a server admin if they want to enable it with `%slevel`",
                CommandHandler.getCommand(LevelCommand.class).getCategory().getPrefix(context.getMessage())
            );
        }

        Collection collection = loadTop100From(context);
        if (collection == null) {
            context.makeWarning("There are no leaderboard data right now, try again later.").queue();
            return false;
        }

        List<String> messages = new ArrayList<>();
        SimplePaginator paginator = new SimplePaginator(collection.getItems(), 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        paginator.forEach((index, key, val) -> {
            DataRow row = (DataRow) val;

            Member member = context.getGuild().getMemberById(row.getLong("user_id"));
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

        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage())));

        context.makeInfo(String.join("\n", messages))
            .setTitle(context.getGuild().getName() + " Leaderboard", "https://avairebot.com/leaderboard/" + context.getGuild().getId())
            .queue();

        return true;
    }

    private Collection loadTop100From(CommandMessage context) {
        return (Collection) avaire.getCache().getAdapter(CacheType.MEMORY).remember("database-xp-leaderboard." + context.getGuild().getId(), 60, () -> {
            return avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .orderBy("experience", "desc")
                .take(100)
                .get();
        });
    }
}
