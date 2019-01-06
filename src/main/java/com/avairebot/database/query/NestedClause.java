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

package com.avairebot.database.query;

import com.avairebot.contracts.database.QueryClause;
import com.avairebot.database.DatabaseManager;

import java.util.List;

public class NestedClause implements QueryClause {

    /**
     * The query builder instance that should be parsed to the clause
     * consumer, used for building the separate where clauses.
     */
    private final QueryBuilder builder;

    /**
     * The operator that should be used along with the nested clauses.
     */
    private final OperatorType operator;

    /**
     * Creates a new nested clause with an {@link OperatorType#AND} operator.
     *
     * @param dbm The database manager instance.
     */
    NestedClause(DatabaseManager dbm) {
        builder = new QueryBuilder(dbm);

        this.operator = OperatorType.AND;
    }

    /**
     * Creates a new nested clause with the provided operator type.
     *
     * @param dbm      The database manager instance.
     * @param operator The operator type to use.
     */
    NestedClause(DatabaseManager dbm, OperatorType operator) {
        builder = new QueryBuilder(dbm);

        this.operator = operator;
    }

    /**
     * Creates a SQL WHERE clause with an equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return the query builder instance.
     */
    public NestedClause where(String column, Object value) {
        return where(column, "=", value);
    }

    /**
     * Creates a SQL WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return the query builder instance.
     */
    public NestedClause where(String column, String operator, Object value) {
        builder.where(column, operator, value);

        return this;
    }

    /**
     * Creates a SQL AND WHERE clause with an equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return the query builder instance.
     */
    public NestedClause andWhere(String column, Object value) {
        return andWhere(column, "=", value);
    }

    /**
     * Creates a SQL AND WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return the query builder instance.
     */
    public NestedClause andWhere(String column, String operator, Object value) {
        builder.andWhere(column, operator, value);

        return this;
    }

    /**
     * Creates a SQL OR WHERE clause with an equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return the query builder instance.
     */
    public NestedClause orWhere(String column, Object value) {
        return orWhere(column, "=", value);
    }

    /**
     * Creates a SQL OR WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return the query builder instance.
     */
    public NestedClause orWhere(String column, String operator, Object value) {
        builder.orWhere(column, operator, value);

        return this;
    }

    /**
     * Gets the list where clauses that should be generated.
     *
     * @return the list of where clauses that should be generated.
     */
    public List<QueryClause> getWhereClauses() {
        return builder.getWhereClauses();
    }

    /**
     * Gets the operator string value.
     *
     * @return the operator string value.
     */
    public String getOperator() {
        return operator.getOperator();
    }

    /**
     * Gets the query builder instance used to build the nested where clauses.
     *
     * @return the query builder instance.
     */
    public QueryBuilder getBuilder() {
        return builder;
    }
}
