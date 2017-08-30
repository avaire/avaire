package com.avairebot.orion.contracts.database.schema;

import com.avairebot.orion.database.schema.Blueprint;

public interface DatabaseClosure {
    public void run(Blueprint table);
}
