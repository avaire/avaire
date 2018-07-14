package com.avairebot.blacklist;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.collection.Collection;
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
        if (blacklist.contains(id)) {
            return;
        }

        blacklist.add(new BlacklistEntity(scope, id));

        try {
            avaire.getDatabase().newQueryBuilder(Constants.BLACKLIST_TABLE_NAME)
                .insert(statement -> {
                    statement.set("id", id);
                    statement.set("type", scope.getId());

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
