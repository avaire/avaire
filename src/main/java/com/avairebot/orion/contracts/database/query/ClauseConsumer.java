package com.avairebot.orion.contracts.database.query;

import com.avairebot.orion.database.query.NestedClause;

public interface ClauseConsumer {

    void build(NestedClause builder);
}
