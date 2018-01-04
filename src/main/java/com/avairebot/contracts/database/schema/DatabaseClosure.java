package com.avairebot.contracts.database.schema;

import com.avairebot.database.schema.Blueprint;

public interface DatabaseClosure {

    void run(Blueprint table);
}
