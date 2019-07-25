/*
 * Copyright (c) 2019.
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

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.modlog.Modlog;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogType;
import com.avairebot.mute.MuteContainer;
import com.avairebot.scheduler.ScheduleHandler;
import com.avairebot.time.Carbon;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DrainMuteQueueTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(DrainMuteQueueTask.class);

    @Override
    public void handle(AvaIre avaire) {
        if (avaire.getMuteManger() == null || avaire.getMuteManger().getMutes().isEmpty()) {
            return;
        }

        for (Map.Entry<Long, HashSet<MuteContainer>> entry : avaire.getMuteManger().getMutes().entrySet()) {
            for (MuteContainer container : entry.getValue()) {
                if (container.isPermanent() || container.getSchedule() != null) {
                    continue;
                }

                Carbon expires = container.getExpiresAt();
                if (expires.copy().subMinutes(5).isPast()) {
                    log.debug("Unmute task started for guildId:{}, userId:{}",
                        container.getGuildId(), container.getUserId()
                    );

                    container.registerSchedule(ScheduleHandler.getScheduler().schedule(
                        () -> handleAutomaticUnmute(avaire, container),
                        expires.diffInSeconds(),
                        TimeUnit.SECONDS
                    ));
                }
            }
        }
    }

    private void handleAutomaticUnmute(AvaIre avaire, MuteContainer container) {
        try {
            avaire.getMuteManger().unregisterMute(container.getGuildId(), container.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Guild guild = avaire.getShardManager().getGuildById(container.getGuildId());
        if (guild == null) {
            return;
        }

        Member member = guild.getMemberById(container.getUserId());
        if (member == null) {
            return;
        }

        GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);
        if (transformer == null) {
            return;
        }

        Role muteRole = guild.getRoleById(transformer.getMuteRole());
        if (muteRole == null) {
            return;
        }

        guild.getController().removeSingleRoleFromMember(
            member, muteRole
        ).queue(aVoid -> {
            ModlogAction modlogAction = new ModlogAction(
                ModlogType.UNMUTE, guild.getSelfMember().getUser(), member.getUser(),
                "Automatic unmute after time expired"
            );

            String caseId = Modlog.log(avaire, guild, transformer, modlogAction);
            Modlog.notifyUser(member.getUser(), guild, modlogAction, caseId);
        });
    }
}
