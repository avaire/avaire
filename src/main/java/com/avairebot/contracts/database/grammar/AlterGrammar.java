package com.avairebot.contracts.database.grammar;

import com.avairebot.database.schema.Blueprint;

public abstract class AlterGrammar extends Grammar {

    /**
     * The query formatter method, this is called by the query
     * builder when the query should be built.
     *
     * @param blueprint The blueprint to build the query from.
     * @return the formatted SQL query
     */
    public abstract String format(Blueprint blueprint);

    /**
     * Adds the last few touches the query needs to be ready to be executed.
     *
     * @param builder The query builder to finalize.
     * @return the finalized SQL query
     */
    protected abstract String finalize(Blueprint builder);
}
