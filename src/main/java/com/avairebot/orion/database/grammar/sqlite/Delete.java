package com.avairebot.orion.database.grammar.sqlite;

import com.avairebot.orion.database.grammar.contracts.DeleteGrammar;
import com.avairebot.orion.database.query.QueryBuilder;

public class Delete extends DeleteGrammar {
    @Override
    public String format(QueryBuilder builder) {
        addPart(String.format(" %s", formatField(builder.getTable())));

        buildWhereClause(builder);

        return finalize(builder);
    }

    @Override
    protected String finalize(QueryBuilder builder) {
        addPart(";");

        return query;
    }
}
