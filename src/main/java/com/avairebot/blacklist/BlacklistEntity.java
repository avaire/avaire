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

import com.avairebot.time.Carbon;

import javax.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class BlacklistEntity {

    private final Scope scope;
    private final long id;
    private final Carbon expiresIn;

    /**
     * Create a new blacklist entity with the given scope, id, and expires time.
     *
     * @param scope     The scope for the blacklist entity.
     * @param id        The ID that the blacklist entity should be linked to.
     * @param expiresIn The carbon time instance for when the blacklist entity should expire.
     */
    BlacklistEntity(Scope scope, long id, @Nullable Carbon expiresIn) {
        this.scope = scope;
        this.id = id;
        this.expiresIn = expiresIn;
    }

    /**
     * Create a new blacklist entity with the given scope, and id. The blacklist
     * entity will be created with a <code>null</code> expire time, making
     * the blacklist entity last forever.
     *
     * @param scope The scope for the blacklist entity.
     * @param id    The ID that the blacklist entity should be linked to.
     */
    public BlacklistEntity(Scope scope, long id) {
        this(scope, id, null);
    }

    /**
     * The scope for the blacklist entity.
     *
     * @return The scope for the blacklist entity.
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * The ID that the blacklist entity is linked to.
     *
     * @return The ID that the blacklist entity is linked to.
     */
    public long getId() {
        return id;
    }

    /**
     * Checks if the blacklist entity is still blacklisted.
     *
     * @return <code>True</code> if the user is still blacklisted, <code>False</code> otherwise.
     */
    public boolean isBlacklisted() {
        return expiresIn == null || expiresIn.isFuture();
    }
}
