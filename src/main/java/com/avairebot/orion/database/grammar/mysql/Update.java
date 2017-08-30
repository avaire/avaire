package com.avairebot.orion.database.grammar.mysql;

import com.avairebot.orion.contracts.database.grammar.UpdateGrammar;
import com.avairebot.orion.database.query.QueryBuilder;

import java.util.List;
import java.util.Map;

public class Update extends UpdateGrammar {
    @Override
    public String format(QueryBuilder builder) {
        addPart(String.format(" %s SET", formatField(builder.getTable())));

        buildKeyset(builder);
        buildValues(builder);
        buildWhereClause(builder);

        return finalize(builder);
    }

    private void buildKeyset(QueryBuilder builder) {
        List<Map<String, Object>> items = builder.getItems();

        items.stream().forEach((map) -> {
            map.keySet().stream().filter((key) -> (!keyset.contains(key))).forEach((key) -> {
                keyset.add(key);
            });
        });

    }

    private void buildValues(QueryBuilder builder) {
        List<Map<String, Object>> items = builder.getItems();

        for (Map<String, Object> row : items) {

            for (String key : keyset) {
                if (!row.containsKey(key)) {
                    addPart("NULL, ");

                    continue;
                }

                String value = row.get(key).toString();
                String formatKey = formatField(key);

                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    addPart(String.format(" %s = %s, ", formatKey, value.equalsIgnoreCase("true") ? 1 : 0));

                    continue;
                }

                if (isNumeric(value)) {
                    addPart(String.format("%s = %s, ", formatKey, value.toUpperCase()));

                    continue;
                }

                addPart(String.format("%s = %s, ", formatKey, value.toUpperCase()));
            }

            removeLast(2).addPart(" ");
        }

        removeLast(1);
    }

    @Override
    protected String finalize(QueryBuilder builder) {
        addPart(";");

        return query;
    }
}
