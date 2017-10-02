package com.avairebot.orion.contracts.database.schema;

import com.avairebot.orion.database.schema.Blueprint;

public interface DatabaseClosure {

    void run(Blueprint table);
}
