package com.avairebot.database.grammar.sqlite;

import com.avairebot.contracts.database.grammar.DeleteGrammar;
import com.avairebot.database.query.QueryBuilder;

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
