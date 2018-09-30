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

    public static int respect = 0;

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
        respect++;

        try {
            avaire.getDatabase().newQueryBuilder(Constants.STATISTICS_TABLE_NAME)
                .useAsync(true)
                .update(statement -> statement.setRaw("respects", "`respects` + 1"));
        } catch (SQLException ex) {
            return false;
        }

        context.makeEmbeddedMessage()
            .setColor(Color.decode("#2A2C31"))
            .setDescription(context.i18n("hasPaidTheirRespects", context.getMember().getEffectiveName()))
            .setFooter(context.i18n("todayAndOverall", NumberUtil.formatNicely(respect), getTotalRespects()))
            .queue();

        return true;
    }

    private String getTotalRespects() {
        return avaire.getCache().remember("rip.total", 10, () -> {
            try {
                return NumberUtil.formatNicely(
                    avaire.getDatabase().newQueryBuilder(Constants.STATISTICS_TABLE_NAME).get().first()
                        .getInt("respects", respect) + 1
                );
            } catch (SQLException e) {
                return "1";
            }
        }).toString();
    }
}
