package com.avairebot.orion.database.query;

import com.avairebot.orion.database.grammar.*;

public enum QueryType {

    SELECT(SelectParser.class),
    INSERT(InsertParser.class),
    UPDATE(UpdateParser.class),
    DELETE(DeleteParser.class),
    CREATE(CreateParser.class);

    private final Class grammar;

    private QueryType(Class grammar) {
        this.grammar = grammar;
    }

    public <T> Class<T> getGrammar() {
        return grammar;
    }
}
