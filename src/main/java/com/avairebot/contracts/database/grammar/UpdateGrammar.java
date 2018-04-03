package com.avairebot.contracts.database.grammar;

import java.util.ArrayList;
import java.util.List;

public abstract class UpdateGrammar extends TableGrammar {

    protected final List<String> keyset = new ArrayList<>();

    public UpdateGrammar() {
        query = "UPDATE ";
    }
}
