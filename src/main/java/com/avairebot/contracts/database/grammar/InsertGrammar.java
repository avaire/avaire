package com.avairebot.contracts.database.grammar;

import java.util.ArrayList;
import java.util.List;

public abstract class InsertGrammar extends TableGrammar {

    protected final List<String> keyset = new ArrayList<>();

    public InsertGrammar() {
        query = "INSERT INTO ";
    }
}
