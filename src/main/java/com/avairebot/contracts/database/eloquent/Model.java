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

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Model extends DefaultFields {

    protected final QueryBuilder builder;

    /**
     * Creates the eloquent model instance.
     *
     * @param dbm The database manager instance used
     *            to communicate with the database
     */
    public Model(DatabaseManager dbm) {
        builder = new QueryBuilder(dbm, table());
    }

    /**
     * Returns the current instance of the eloquent model.
     *
     * @return The current instance of the eloquent model.
     */
    @Override
    public Model instance() {
        return this;
    }

    /**
     * Tries to find the record using the {@link #primaryKey() primary key}
     * and the given value, if no records were found an empty
     * {@link Collection} will be returned instead.
     *
     * @param id The ID that should be compared with the primary key
     * @return An empty collection if no results were found, or a
     * collection that matches the given ID and primary key.
     * @throws SQLException
     */
    public Collection find(Object id) throws SQLException {
        builder.table(table()).where(primaryKey(), id);

        return get();
    }

    /**
     * Gets all the records matching the eloquent model type from the database.
     *
     * @return All records matching the eloquent model in a collection, if the
     * table has no records an empty collection will be returned instead.
     */
    public Collection all() throws SQLException {
        builder.table(table()).selectAll();

        return get();
    }

    /**
     * Selects the provided columns from the varargs columns object, columns
     * parsed will automatically be formatted to SQL fields using the grave
     * accent character(`), using the keyword <code>AS</code> will use the
     * SQL AS to rename the output the collection will end up using.
     * <p>
     * Example calling: <code>select("username as name", "email", ...)</code>
     * <p>
     * Would re-name the username field in the table to name in the collection object.
     *
     * @param columns The varargs list of columns that should be selected
     * @return The eloquent query model instance.
     */
    public Model select(String... columns) {
        builder.select(columns);

        return this;
    }

    /**
     * Creates a SQL WHERE clause with an equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return The eloquent query model instance.
     */
    public Model where(String column, Object value) {
        return where(column, "=", value);
    }

    /**
     * Creates a SQL WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return The eloquent query model instance.
     */
    public Model where(String column, String operator, Object value) {
        builder.where(column, operator, value);

        return this;
    }

    /**
     * Creates a SQL AND WHERE clause with an equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return The eloquent query model instance.
     */
    public Model andWhere(String column, Object value) {
        return andWhere(column, "=", value);
    }

    /**
     * Creates a SQL AND WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return The eloquent query model instance.
     */
    public Model andWhere(String column, String operator, Object value) {
        builder.andWhere(column, operator, value);

        return this;
    }

    /**
     * Creates a SQL OR WHERE clause with the equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return The eloquent query model instance.
     */
    public Model orWhere(String column, Object value) {
        return orWhere(column, "=", value);
    }

    /**
     * Creates a SQL OR WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return The eloquent query model instance.
     */
    public Model orWhere(String column, String operator, Object value) {
        builder.orWhere(column, operator, value);

        return this;
    }

    /**
     * Creates a LEFT JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return The eloquent query model instance.
     */
    public Model leftJoin(String table, String one, String two) {
        builder.leftJoin(table, one, two);

        return this;
    }

    /**
     * Creates a LEFT JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return The eloquent query model instance.
     */
    public Model leftJoin(String table, String one, String operator, String two) {
        builder.leftJoin(table, one, operator, two);

        return this;
    }

    /**
     * Creates a RIGHT JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return The eloquent query model instance.
     */
    public Model rightJoin(String table, String one, String two) {
        builder.rightJoin(table, one, two);

        return this;
    }

    /**
     * Creates a RIGHT JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return The eloquent query model instance.
     */
    public Model rightJoin(String table, String one, String operator, String two) {
        builder.rightJoin(table, one, operator, two);

        return this;
    }

    /**
     * Creates a INNER JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return The eloquent query model instance.
     */
    public Model innerJoin(String table, String one, String two) {
        builder.innerJoin(table, one, two);

        return this;
    }

    /**
     * Creates a INNER JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return The eloquent query model instance.
     */
    public Model innerJoin(String table, String one, String operator, String two) {
        builder.innerJoin(table, one, operator, two);

        return this;
    }

    /**
     * Creates a OUTER JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return The eloquent query model instance.
     */
    public Model outerJoin(String table, String one, String two) {
        builder.outerJoin(table, one, two);

        return this;
    }

    /**
     * Creates a OUTER JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return The eloquent query model instance.
     */
    public Model outerJoin(String table, String one, String operator, String two) {
        builder.outerJoin(table, one, operator, two);

        return this;
    }

    /**
     * Selects another eloquent model with the current model query by
     * using joins, the given fields must be the name of methods
     * that uses the {@link QueryScope query scope}.
     *
     * @param fields The names of the methods used for
     * @return The eloquent query model instance.
     */
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

    /**
     * Runs the {@link QueryBuilder#get()} method with the generated query.
     *
     * @return a <code>Collection</code> object that contains the data produced
     * by the given query; never <code>null</code>@exception
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public Collection get() throws SQLException {
        return builder.get();
    }

    @Override
    public String toString() {
        return builder.toSQL();
    }
}
