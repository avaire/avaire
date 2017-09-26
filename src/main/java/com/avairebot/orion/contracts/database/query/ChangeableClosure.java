package com.avairebot.orion.contracts.database.query;

import com.avairebot.orion.database.query.ChangeableStatement;

public interface ChangeableClosure {

    void run(ChangeableStatement statement);
}
