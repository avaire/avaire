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

public class BlacklistEntity {

    private final Scope scope;
    private final long id;
    private final Carbon expiresIn;

    public BlacklistEntity(Scope scope, long id, Carbon expiresIn) {
        this.scope = scope;
        this.id = id;
        this.expiresIn = expiresIn;
    }

    public BlacklistEntity(Scope scope, long id) {
        this(scope, id, null);
    }

    public Scope getScope() {
        return scope;
    }

    public long getId() {
        return id;
    }

    public boolean isExpired() {
        return expiresIn == null || expiresIn.isFuture();
    }
}
