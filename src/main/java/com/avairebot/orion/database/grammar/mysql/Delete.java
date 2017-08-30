package com.avairebot.orion.database.grammar.mysql;

import com.avairebot.orion.contracts.database.grammar.DeleteGrammar;
import com.avairebot.orion.database.query.QueryBuilder;

public class Delete extends DeleteGrammar {
    @Override
    public String format(QueryBuilder builder) {
        String table = builder.getTable();

        addPart(String.format(" %s", formatField(table)));

        buildWhereClause(builder);

        return finalize(builder);
    }

    @Override
    protected String finalize(QueryBuilder builder) {
        addPart(";");

        return query;
    }
}
