package com.avairebot.contracts.database.query;

import com.avairebot.database.query.NestedClause;

public interface ClauseConsumer {

    void build(NestedClause builder);
}
