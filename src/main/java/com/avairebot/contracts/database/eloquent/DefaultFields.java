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

package com.avairebot.contracts.database.eloquent;

import com.avairebot.database.schema.Blueprint;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class DefaultFields {

    /**
     * Returns the current instance of the eloquent model.
     *
     * @return The current instance of the eloquent model.
     */
    public abstract Model instance();

    /**
     * Generates the name of the table that should be
     * used for queries for the model type.
     *
     * @return The name of the table used for model queries.
     */
    protected String table() {
        return getClass().getSimpleName() + "s";
    }

    /**
     * Gets the primary key of the table used for model queries, the primary
     * key is used for the {@link Model#find(Object)} method, by default
     * the primary key will be {@code ID}.
     *
     * @return The primary key for the table used for model queries.
     */
    protected String primaryKey() {
        return "id";
    }

    /**
     * Determines if the {@link #primaryKey() primary key}
     * is auto incrementing or not.
     *
     * @return {@code True} if the primary key is auto incrementing, {@code False} otherwise.
     */
    protected boolean incrementing() {
        return true;
    }

    /**
     * Determines if the {@link #table() model table} uses timestamps,
     * the timestamps are generated using the schema migrations using
     * the {@link Blueprint#Timestamps() timestamps} method.
     *
     * @return {@code True} if the model table uses timestamps, {@code False} otherwise.
     */
    protected boolean timestamps() {
        return true;
    }
}
