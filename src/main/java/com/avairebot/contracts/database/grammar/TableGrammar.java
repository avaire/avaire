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

package com.avairebot.contracts.database.grammar;

import com.avairebot.contracts.database.QueryClause;
import com.avairebot.database.exceptions.DatabaseException;
import com.avairebot.database.query.Clause;
import com.avairebot.database.query.NestedClause;
import com.avairebot.database.query.OperatorType;
import com.avairebot.database.query.QueryBuilder;

import java.sql.SQLException;

public abstract class TableGrammar extends Grammar {

    /**
     * The query formatter method, this is called by the query
     * builder when the query should be built.
     *
     * @param builder The query builder to format.
     * @return the formatted query
     */
    public abstract String format(QueryBuilder builder);

    /**
     * Adds the last few touches the query needs to be ready to be executed.
     *
     * @param builder The query builder to finalize.
     * @return the finalized query
     */
    protected abstract String finalize(QueryBuilder builder);

    /**
     * builds the where clauses for the provided query builder.
     *
     * @param builder the query builder to build the where clauses from
     */
    protected void buildWhereClause(QueryBuilder builder) {
        if (builder.getWhereClauses().isEmpty()) {
            return;
        }

        addPart(" WHERE ");
        boolean first = true;

        for (QueryClause obj : builder.getWhereClauses()) {
            // This will build a normal clause
            if (obj instanceof Clause) {
                Clause clause = (Clause) obj;

                addClause(clause, first);
                first = false;

                continue;
            }

            // This will build a nested clause
            if (obj instanceof NestedClause) {
                NestedClause nestedClause = (NestedClause) obj;

                if (nestedClause.getWhereClauses().isEmpty()) {
                    continue;
                }

                first = true;
                addPart(" %s (", nestedClause.getOperator());

                for (QueryClause temp : nestedClause.getWhereClauses()) {
                    if (!(temp instanceof Clause)) {
                        continue;
                    }

                    Clause clause = (Clause) temp;

                    addClause(clause, first);
                    first = false;
                }

                addPart(") ");
            }

            first = false;
        }
    }

    private void addClause(Clause clause, boolean exemptOperator) {
        if (clause.getOrder() == null) {
            clause.setOrder(OperatorType.AND);
        }

        if (clause.getTwo() == null) {
            throw new DatabaseException("Invalid 2nd clause given, the clause comparator can not be NULL! Query so far:  " + getQuery(),
                new SQLException("Invalid 2nd clause given, the clause comparator can not be NULL!")
            );
        }

        String field = clause.getTwo().toString();
        if (!isNumeric(field)) {
            field = String.format("'%s'", field);
        }

        String stringClause = String.format("%s %s %s", formatField(clause.getOne()), clause.getIdentifier(), field);

        String operator = "";
        if (!exemptOperator) {
            operator = clause.getOrder().getOperator() + " ";
        }

        addRawPart(String.format("%s%s ", operator, stringClause));
    }
}
