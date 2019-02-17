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
    private final Ratelimit ratelimit;

    /**
     * Creates a new blacklist instance.
     *
     * @param avaire The main avaire instance.
     */
    public Blacklist(AvaIre avaire) {
        this.avaire = avaire;

        this.blacklist = new BlacklistList();
        this.ratelimit = new Ratelimit(this);
    }

    /**
     * Gets the ratelimiter instance for the current blacklist.
     *
     * @return The ratelimiter instance for the current blacklist.
     */
    public Ratelimit getRatelimit() {
        return ratelimit;
    }

    /**
     * Checks if the given ID is blacklisted.
     *
     * @param id Checks if the given ID is blacklisted.
     * @return <code>True</code> if the ID is on the blacklist, <code>False</code> otherwise.
     */
    public boolean isBlacklisted(long id) {
        return blacklist.contains(id);
    }

    /**
     * Checks if the author of the message, of the message was
     * sent in a guild, that the guild is on the blacklist.
     *
     * @param message The message that should be checked.
     * @return <code>True</code> if either the user, or the entire
     * server is blacklisted, <code>False</code> otherwise.
     */
    public boolean isBlacklisted(@Nonnull Message message) {
        return isBlacklisted(message.getAuthor())
            || (message.getChannelType().isGuild()
            && isBlacklisted(message.getGuild()));
    }

    /**
     * Checks if the given user is on the blacklist.
     *
     * @param user The user that should be checked.
     * @return <code>True</code> if the user is on the blacklist, <code>False</code> otherwise.
     */
    public boolean isBlacklisted(@Nonnull User user) {
        if (avaire.getBotAdmins().getUserById(user.getIdLong(), true).isAdmin()) {
            return false;
        }

        BlacklistEntity entity = getEntity(user.getIdLong(), Scope.USER);
        return entity != null && entity.isBlacklisted();
    }

    /**
     * Checks if the given guild is on the blacklist.
     *
     * @param guild The guild that should be checked.
     * @return <code>True</code> if the guild is on the blacklist, <code>False</code> otherwise.
     */
    public boolean isBlacklisted(@Nonnull Guild guild) {
        BlacklistEntity entity = getEntity(guild.getIdLong(), Scope.GUILD);
        return entity != null && entity.isBlacklisted();
    }

    /**
     * Adds the given user to the blacklist with the given reason, the blacklist
     * record will last until it is {@link #remove(long) removed}.
     *
     * @param user   The user that should be added to the blacklist.
     * @param reason The reason for the user being added to the blacklist.
     */
    public void addUser(@Nonnull User user, @Nullable String reason) {
        addIdToBlacklist(Scope.USER, user.getIdLong(), reason);
    }

    /**
     * Adds the given guild to the blacklist with the given reason, the blacklist
     * record will last until it is {@link #remove(long) removed}.
     *
     * @param guild  The guild that should be added to the blacklist.
     * @param reason The reason for the guild being added to the blacklist.
     */
    public void addGuild(@Nonnull Guild guild, @Nullable String reason) {
        addIdToBlacklist(Scope.GUILD, guild.getIdLong(), reason);
    }

    /**
     * Removes the blacklist record with the given ID.
     *
     * @param id The ID to remove from teh blacklist.
     */
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

    /**
     * Gets the blacklist entity for the given ID.
     *
     * @param id The ID to get the blacklist entity for.
     * @return Possibly-null, the blacklist entity matching the given ID.
     */
    @Nullable
    public BlacklistEntity getEntity(long id) {
        return getEntity(id, null);
    }

    /**
     * Gets the blacklist entity for the given ID, matching the given scope.
     *
     * @param id    The ID to get the blacklist entity for.
     * @param scope The scope that the blacklist entity should belong to.
     * @return Possible-null, the blacklist entity matching the given ID and scope.
     */
    @Nullable
    public BlacklistEntity getEntity(long id, @Nullable Scope scope) {
        for (BlacklistEntity entity : blacklist) {
            if (entity.getId() == id) {
                if (scope != null && scope != entity.getScope()) {
                    continue;
                }
                return entity;
            }
        }
        return null;
    }

    /**
     * Adds the ID to the blacklist with the given scope and reason.
     *
     * @param scope  The scope to register the blacklist record under.
     * @param id     The ID that should be added to the blacklist.
     * @param reason The reason that the ID was added to the blacklist.
     */
    public void addIdToBlacklist(Scope scope, final long id, final @Nullable String reason) {
        addIdToBlacklist(scope, id, reason, null);
    }

    /**
     * Adds the ID to the blacklist with the given scope, reason, and expire time.
     *
     * @param scope     The scope to register the blacklist record under.
     * @param id        The ID that should be added to the blacklist.
     * @param reason    The reason that the ID was added to the blacklist.
     * @param expiresIn The carbon time instance for when the entity should expire.
     */
    public void addIdToBlacklist(Scope scope, final long id, final @Nullable String reason, @Nullable Carbon expiresIn) {
        BlacklistEntity entity = getEntity(id, scope);
        if (entity != null) {
            blacklist.remove(entity);
        }

        blacklist.add(new BlacklistEntity(scope, id, reason, expiresIn));

        try {
            avaire.getDatabase().newQueryBuilder(Constants.BLACKLIST_TABLE_NAME)
                .where("id", id).andWhere("type", scope.getId())
                .delete();

            avaire.getDatabase().newQueryBuilder(Constants.BLACKLIST_TABLE_NAME)
                .useAsync(true)
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

    /**
     * Get the all the entities currently on the blacklist, this
     * includes both users and guilds, the type can be checked
     * through the {@link BlacklistEntity#getScope() scope}.
     *
     * @return The entities currently on the blacklist.
     */
    public List<BlacklistEntity> getBlacklistEntities() {
        return blacklist;
    }

    /**
     * Syncs the blacklist with the database.
     */
    public synchronized void syncBlacklistWithDatabase() {
        blacklist.clear();
        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.BLACKLIST_TABLE_NAME)
                .where("expires_in", ">", Carbon.now())
                .get();

            collection.forEach(row -> {
                String id = row.getString("id", null);
                if (id == null) {
                    return;
                }

                try {
                    long longId = Long.parseLong(id);
                    Scope scope = Scope.fromId(row.getInt("type", 0));

                    blacklist.add(new BlacklistEntity(
                        scope, longId,
                        row.getString("reason"),
                        row.getTimestamp("expires_in")
                    ));
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
