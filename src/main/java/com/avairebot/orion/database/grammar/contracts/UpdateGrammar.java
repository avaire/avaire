package com.avairebot.orion.database.grammar.contracts;

import com.avairebot.orion.contracts.database.grammar.TableGrammar;

import java.util.ArrayList;
import java.util.List;

public abstract class UpdateGrammar extends TableGrammar {
    protected final List<String> keyset = new ArrayList<>();

    public UpdateGrammar() {
        query = "UPDATE ";
    }
}
