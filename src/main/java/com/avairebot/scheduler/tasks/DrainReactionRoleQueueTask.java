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

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DrainReactionRoleQueueTask implements Task {

    private static final DelayQueue<ReactionActionEntity> queue = new DelayQueue<>();
    private static long lastCheck = -1;

    /**
     * Queues the given reaction action entity, adding or
     * removing the role for the user in the entity.
     *
     * @param entity The reaction action entity that should be added to the queue.
     */
    public static void queueReactionActionEntity(ReactionActionEntity entity) {
        if (queue.contains(entity)) {
            queue.remove(entity);
        }

        long diff = 0;
        if (lastCheck > System.currentTimeMillis()) {
            diff = lastCheck - System.currentTimeMillis();
            entity.setDuration(diff + ReactionActionEntity.defaultDuration);
        }
        lastCheck = System.currentTimeMillis() + diff + ReactionActionEntity.defaultDuration;

        queue.put(entity);
    }

    @Override
    public void handle(AvaIre avaire) {
        if (queue.isEmpty()) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            ReactionActionEntity entity = queue.poll();
            if (entity == null) {
                return;
            }

            run(avaire, entity);
        }
    }

    private void run(AvaIre avaire, ReactionActionEntity entity) {
        Guild guild = avaire.getShardManager().getGuildById(entity.guildId);
        if (guild == null) {
            return;
        }

        Member member = guild.getMemberById(entity.userId);
        if (member == null) {
            return;
        }

        Role role = guild.getRoleById(entity.roleId);
        if (role == null) {
            return;
        }

        switch (entity.type) {
            case ADD:
                if (RoleUtil.hasRole(member, role)) {
                    return;
                }
                guild.getController().addRolesToMember(member, role).queue();
                break;

            case REMOVE:
                if (!RoleUtil.hasRole(member, role)) {
                    return;
                }
                guild.getController().removeRolesFromMember(member, role).queue();
                break;
        }
    }

    /**
     * The type of reaction action that should be preformed.
     */
    public enum ReactionActionType {
        ADD, REMOVE
    }

    public static class ReactionActionEntity implements Delayed {

        /**
         * The default time in milliseconds that should be between reaction checks
         * that are sent to the Discords API, their ratelimit allows one request
         * a second, per server, or sixty requests every minute.
         */
        static final long defaultDuration = 500;

        private final long guildId;
        private final long userId;
        private final long roleId;
        private final ReactionActionType type;
        private long duration;

        public ReactionActionEntity(long guildId, long userId, long roleId, ReactionActionType type) {
            this.guildId = guildId;
            this.userId = userId;
            this.roleId = roleId;
            this.type = type;
        }

        /**
         * Sets the duration in milliseconds for when the reaction entity can be consumed
         * by the {@link DrainReactionRoleQueueTask drain reaction queue task}, the duration
         * will be added to the current time in milliseconds.
         *
         * @param duration The time in milliseconds before the vote entity can be consumed.
         */
        public void setDuration(long duration) {
            this.duration = System.currentTimeMillis() + duration;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ReactionActionEntity
                && ((ReactionActionEntity) obj).userId == userId
                && ((ReactionActionEntity) obj).roleId == roleId;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(duration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NotNull Delayed obj) {
            return (int) (this.duration - ((ReactionActionEntity) obj).duration);
        }

        @Override
        public String toString() {
            return "ReactionActionEntity [duration=" + duration + ", userId=" + userId + ", roleId=" + roleId + ", type=" + type.name() + "]";
        }
    }
}
