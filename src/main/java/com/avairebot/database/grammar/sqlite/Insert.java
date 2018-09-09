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

import com.avairebot.contracts.database.grammar.InsertGrammar;
import com.avairebot.database.query.QueryBuilder;

import java.util.List;
import java.util.Map;

public class Insert extends InsertGrammar {

    @Override
    public String format(QueryBuilder builder) {
        addPart(String.format(" %s", formatField(builder.getTable())));

        buildKeyset(builder);
        buildValues(builder);

        return finalize(builder);
    }

    private void buildKeyset(QueryBuilder builder) {
        List<Map<String, Object>> items = builder.getItems();

        items.stream().forEach((map) -> {
            map.keySet().stream().filter((key) -> (!keyset.contains(key))).forEach((key) -> {
                keyset.add(key);
            });
        });

        addPart(" (");

        keyset.stream().forEach((key) -> {
            addPart(String.format("`%s`, ", key));
        });

        removeLast(2).addPart(")");
    }

    private void buildValues(QueryBuilder builder) {
        addPart(" VALUES ");

        List<Map<String, Object>> items = builder.getItems();

        for (Map<String, Object> row : items) {
            addPart(" (");

            for (String key : keyset) {
                if (!row.containsKey(key)) {
                    addPart("NULL, ");

                    continue;
                }

                if (row.get(key) == null) {
                    addPart("NULL, ");

                    continue;
                }

                String value = row.get(key).toString();

                if (value.startsWith("RAW:")) {
                    addPart("%s, ", value.substring(4));

                    continue;
                }

                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    addPart(String.format("%s, ", value.equalsIgnoreCase("true") ? 1 : 0));

                    continue;
                }

                if (isNumeric(value)) {
                    addPart(String.format("'%s', ", value));

                    continue;
                }

                addPart("?, ");
            }

            removeLast(2).addPart("),");
        }

        removeLast(1);
    }

    @Override
    protected String finalize(QueryBuilder builder) {
        addPart(";");

        return query;
    }
}
