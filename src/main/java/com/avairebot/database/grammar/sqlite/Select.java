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

package com.avairebot.database.grammar.sqlite;

import com.avairebot.contracts.database.grammar.SelectGrammar;
import com.avairebot.database.query.*;

import java.util.List;

public class Select extends SelectGrammar {

    @Override
    public String format(QueryBuilder builder) {
        buildColumns(builder);

        buildJoins(builder);

        buildWhereClause(builder);

        return finalize(builder);
    }

    private void buildColumns(QueryBuilder builder) {
        if (builder.getColumns().isEmpty() || builder.getColumns().size() == 1 && builder.getColumns().get(0).equals("*")) {
            query += String.format("* FROM %s ", formatField(builder.getTable()));
            return;
        }

        builder.getColumns().stream().forEach((column) -> {
            if (column.startsWith("RAW:")) {
                query += String.format("%s, ", column.substring(4));
                return;
            }
            query += formatField(column) + ", ";
        });

        removeLast(2);

        query += String.format(" FROM %s ", formatField(builder.getTable()));
    }

    private void buildJoins(QueryBuilder builder) {
        List<JoinClause> joins = builder.getJoins();

        for (JoinClause join : joins) {
            if (join.clauses.isEmpty()) {
                continue;
            }

            addPart(String.format(" %s JOIN %s ON ", join.type.toUpperCase(), formatField(join.table)));

            int orderLength = 0;

            for (Clause clause : join.clauses) {
                String string = String.format(" %s %s %s",
                    formatField(clause.getOne()), clause.getIdentifier(), formatField((String) clause.getTwo())
                );

                if (clause.getOrder() == null) {
                    clause.setOrder(OperatorType.AND);
                }

                String operator = clause.getOrder().getOperator();

                orderLength = operator.length() + 2;
                addPart(String.format(string + " %s ", operator));
            }

            if (orderLength > 0) {
                removeLast(orderLength);
            }
        }
    }

    @Override
    protected String finalize(QueryBuilder builder) {
        if (!builder.getOrder().isEmpty()) {
            addPart(" ORDER BY ");

            for (QueryOrder order : builder.getOrder()) {
                if (order.getField() == null) {
                    continue;
                }

                if (order.isRawSQL()) {
                    addPart(" %s , ", order.getField());

                    continue;
                }

                if (order.getType() != null) {
                    addPart(" %s %s, ", formatField(order.getField()), order.getType().toUpperCase());
                }
            }

            removeLast(2);
        }

        if (builder.getTake() <= 0) {
            return query.trim() + ";";
        }

        if (builder.getTake() > 0) {
            addPart(String.format(" LIMIT %d", builder.getTake()));

            // The skip clause is placed inside the limit statement because LIMIT is 
            // required for the OFFSET to be regonized by the SQL server, placing
            // it outside will throw a Syntex Exception due to the missing limit.
            if (builder.getSkip() > 0) {
                addPart(String.format(" OFFSET %d", builder.getSkip()));
            }
        }

        addPart(";");

        return query;
    }
}
