package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.NumberUtil;

import java.awt.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class RipCommand extends Command {

    public static int RESPECT = 0;

    public RipCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "RIP Command";
    }

    @Override
    public String getDescription() {
        return "Pay your respects";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Pay your respects");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("rip");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        RESPECT++;

        try {
            avaire.getDatabase().newQueryBuilder(Constants.STATISTICS_TABLE_NAME)
                .useAsync(true)
                .update(statement -> statement.setRaw("respects", "`respects` + 1"));
        } catch (SQLException ex) {
            return false;
        }

        context.makeEmbeddedMessage()
            .setColor(Color.decode("#2A2C31"))
            .setDescription(String.format(context.i18n("hasPaidTheirRespects"), context.getMember().getEffectiveName()))
            .setFooter(String.format(context.i18n("todayAndOverall"), NumberUtil.formatNicely(RESPECT), getTotalRespects()))
            .queue();

        return true;
    }

    private String getTotalRespects() {
        return avaire.getCache().remember("rip.total", 10, () -> {
            try {
                return NumberUtil.formatNicely(
                    avaire.getDatabase().newQueryBuilder(Constants.STATISTICS_TABLE_NAME).get().first()
                        .getInt("respects", RESPECT) + 1
                );
            } catch (SQLException e) {
                return "1";
            }
        }).toString();
    }
}
