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

import com.avairebot.database.DatabaseManager;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.query.QueryBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Model extends DefaultFields {

    protected final QueryBuilder builder;

    public Model(DatabaseManager dbm) {
        builder = new QueryBuilder(dbm, table());
    }

    @Override
    public Model instance() {
        return this;
    }

    public Collection find(int id) throws SQLException {
        builder.table(table()).where(primaryKey(), id);

        return get();
    }

    public Collection all() throws SQLException {
        builder.table(table()).selectAll();

        return get();
    }

    public Model select(String... columns) {
        builder.select(columns);

        return this;
    }

    public Model where(String column, Object field) {
        return where(column, "=", field);
    }

    public Model where(String column, String identifier, Object field) {
        builder.where(column, identifier, field);

        return this;
    }

    public Model andWhere(String column, Object field) {
        return andWhere(column, "=", field);
    }

    public Model andWhere(String column, String identifier, Object field) {
        builder.andWhere(column, identifier, field);

        return this;
    }

    public Model orWhere(String column, Object field) {
        return orWhere(column, "=", field);
    }

    public Model orWhere(String column, String identifier, Object field) {
        builder.orWhere(column, identifier, field);

        return this;
    }

    public Model leftJoin(String table, String one, String two) {
        builder.leftJoin(table, one, two);

        return this;
    }

    public Model leftJoin(String table, String one, String identifier, String two) {
        builder.leftJoin(table, one, identifier, two);

        return this;
    }

    public Model rightJoin(String table, String one, String two) {
        builder.rightJoin(table, one, two);

        return this;
    }

    public Model rightJoin(String table, String one, String identifier, String two) {
        builder.rightJoin(table, one, identifier, two);

        return this;
    }

    public Model innerJoin(String table, String one, String two) {
        builder.innerJoin(table, one, two);

        return this;
    }

    public Model innerJoin(String table, String one, String identifier, String two) {
        builder.innerJoin(table, one, identifier, two);

        return this;
    }

    public Model outerJoin(String table, String one, String two) {
        builder.outerJoin(table, one, two);

        return this;
    }

    public Model outerJoin(String table, String one, String identifier, String two) {
        builder.outerJoin(table, one, identifier, two);

        return this;
    }

    public Model with(String... fields) {
        List<String> methods = new ArrayList<>();
        methods.addAll(Arrays.asList(fields));

        Class<? extends Model> iClass = instance().getClass();

        for (Method method : iClass.getMethods()) {
            if (!methods.contains(method.getName())) {
                continue;
            }

            if (method.getAnnotations().length == 0) {
                continue;
            }

            boolean isQueryHandler = false;
            for (Annotation an : method.getAnnotations()) {
                if (an.annotationType().getName().equals(QueryScope.class.getName())) {
                    isQueryHandler = true;
                    break;
                }
            }

            if (isQueryHandler) {
                try {
                    method.setAccessible(true);
                    method.invoke(instance(), builder);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return this;
    }

    public Collection get() throws SQLException {
        return builder.get();
    }

    @Override
    public String toString() {
        return builder.toSQL();
    }
}
