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

package com.avairebot.blacklist;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.query.ChangeableStatement;
import com.avairebot.time.Carbon;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("SuspiciousMethodCalls")
public class Blacklist {

    private final AvaIre avaire;
    private final List<BlacklistEntity> blacklist;

    public Blacklist(AvaIre avaire) {
        this.avaire = avaire;

        this.blacklist = new BlacklistList();
    }

    public boolean isBlacklisted(long id) {
        return blacklist.contains(id);
    }

    public boolean isBlacklisted(@Nonnull Message message) {
        if (avaire.getBotAdmins().contains(message.getAuthor().getId())) {
            return false;
        }

        return isBlacklisted(message.getAuthor())
            || (message.getChannelType().isGuild()
            && isBlacklisted(message.getGuild()));
    }

    public boolean isBlacklisted(@Nonnull User user) {
        if (avaire.getBotAdmins().contains(String.valueOf(user.getIdLong()))) {
            return false;
        }
        return blacklist.contains(user.getIdLong());
    }

    public boolean isBlacklisted(@Nonnull Guild user) {
        return blacklist.contains(user.getIdLong());
    }

    public void addUser(@Nonnull User user, @Nullable String reason) {
        addIdToBlacklist(Scope.USER, user.getIdLong(), reason);
    }

    public void addGuild(@Nonnull Guild guild, @Nullable String reason) {
        addIdToBlacklist(Scope.GUILD, guild.getIdLong(), reason);
    }

    public void remove(long id) {
        if (!blacklist.contains(id)) {
            return;
        }

        Iterator<BlacklistEntity> iterator = blacklist.iterator();
        while (iterator.hasNext()) {
            BlacklistEntity next = iterator.next();

            if (next.getId() == id) {
                iterator.remove();
                break;
            }
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.BLACKLIST_TABLE_NAME)
                .where("id", id)
                .delete();
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to sync blacklist with the database: " + e.getMessage(), e);
        }
    }

    @Nullable
    public BlacklistEntity getEntity(long id) {
        for (BlacklistEntity entity : blacklist) {
            if (entity.getId() == id) {
                return entity;
            }
        }
        return null;
    }

    public void addIdToBlacklist(Scope scope, final long id, final @Nullable String reason) {
        addIdToBlacklist(scope, id, reason, null);
    }

    public void addIdToBlacklist(Scope scope, final long id, final @Nullable String reason, @Nullable Carbon expiresIn) {
        if (blacklist.contains(id)) {
            return;
        }

        blacklist.add(new BlacklistEntity(scope, id, expiresIn));

        try {
            avaire.getDatabase().newQueryBuilder(Constants.BLACKLIST_TABLE_NAME)
                .insert((ChangeableStatement statement) -> {
                    statement.set("id", id);
                    statement.set("type", scope.getId());
                    statement.set("expires_in", expiresIn);

                    if (expiresIn == null) {
                        statement.set("expires_in", Carbon.now().addYears(10));
                    }

                    if (reason != null) {
                        statement.set("reason", reason);
                    }
                });
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to sync blacklist with the database: " + e.getMessage(), e);
        }
    }

    public void syncBlacklistWithDatabase() {
        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.BLACKLIST_TABLE_NAME).get();

            collection.forEach(row -> {
                String id = row.getString("id", null);
                if (id == null) {
                    return;
                }

                try {
                    long longId = Long.parseLong(id);
                    Scope scope = Scope.fromId(row.getInt("type", 0));

                    blacklist.add(new BlacklistEntity(scope, longId));
                } catch (NumberFormatException ignored) {
                    // This is ignored
                }
            });
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to sync blacklist with the database: " + e.getMessage(), e);
        }
    }

    private class BlacklistList extends ArrayList<BlacklistEntity> {

        @Override
        public boolean contains(Object o) {
            if (o instanceof Long) {
                long id = (long) o;
                for (BlacklistEntity entity : this) {
                    if (entity.getId() == id) {
                        return true;
                    }
                }
                return false;
            }

            return super.contains(o);
        }
    }
}
