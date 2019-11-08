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
import com.avairebot.language.I18n;
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
                //noinspection ConstantConditions
                if (expires.copy().subMinutes(5).isPast()) {
                    long differenceInSeconds = expires.getTimestamp() - getCurrentTimestamp();
                    if (differenceInSeconds < 1) {
                        differenceInSeconds = 1;
                    }

                    log.debug("Unmute task started for guildId:{}, userId:{}, time:{}",
                        container.getGuildId(), container.getUserId(), differenceInSeconds
                    );

                    container.setSchedule(ScheduleHandler.getScheduler().schedule(
                        () -> handleAutomaticUnmute(avaire, container),
                        differenceInSeconds,
                        TimeUnit.SECONDS
                    ));
                }
            }
        }
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    private void handleAutomaticUnmute(AvaIre avaire, MuteContainer container) {
        try {
            Guild guild = avaire.getShardManager().getGuildById(container.getGuildId());
            if (guild == null) {
                if (avaire.areWeReadyYet()) {
                    unregisterDatabaseRecord(avaire, container);
                }

                container.cancelSchedule();
                return;
            }

            unregisterDatabaseRecord(avaire, container);

            Member member = guild.getMemberById(container.getUserId());
            if (member == null) {
                return;
            }

            GuildTransformer transformer = GuildController.fetchGuild(avaire, guild);
            if (transformer == null || transformer.getMuteRole() == null) {
                return;
            }

            Role muteRole = guild.getRoleById(transformer.getMuteRole());
            if (muteRole == null) {
                return;
            }

            guild.getController().removeSingleRoleFromMember(
                member, muteRole
            ).queueAfter(1, TimeUnit.SECONDS, aVoid -> {
                log.debug("Successfully removed the {} role from {} on the {} server.",
                    muteRole.getName(), member.getUser().getAsTag(), guild.getName()
                );

                ModlogAction modlogAction = new ModlogAction(
                    ModlogType.UNMUTE, guild.getSelfMember().getUser(), member.getUser(),
                    I18n.getString(guild, "administration.UnmuteCommand.userAutoUnmutedReason")
                );

                String caseId = Modlog.log(avaire, guild, transformer, modlogAction);
                Modlog.notifyUser(member.getUser(), guild, modlogAction, caseId);
            }, throwable -> {
                log.debug("Failed to remove role from {} on the {} guild, error: {}",
                    container.getUserId(), container.getGuildId(), throwable.getMessage(), throwable
                );
            });
        } catch (Exception e) {
            log.error("Something went wrong in the auto unmute: {}", e.getMessage(), e);
        }
    }

    private void unregisterDatabaseRecord(AvaIre avaire, MuteContainer container) {
        try {
            avaire.getMuteManger().unregisterMute(container.getGuildId(), container.getUserId());
        } catch (SQLException e) {
            log.error("Failed to unregister mute for guildId:{}, userId:{}",
                container.getGuildId(), container.getUserId(), e
            );
        }
    }
}
