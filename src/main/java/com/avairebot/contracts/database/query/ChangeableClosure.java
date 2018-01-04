package com.avairebot.contracts.database.query;

import com.avairebot.database.query.ChangeableStatement;

public interface ChangeableClosure {

    void run(ChangeableStatement statement);
}
