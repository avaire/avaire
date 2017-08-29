package com.avairebot.orion.database.schema.contracts;

import com.avairebot.orion.database.schema.Blueprint;

public interface DatabaseClosure {
    public void run(Blueprint table);
}
