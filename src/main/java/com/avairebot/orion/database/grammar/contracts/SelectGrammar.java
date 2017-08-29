package com.avairebot.orion.database.grammar.contracts;

import com.avairebot.orion.contracts.database.grammar.TableGrammar;

public abstract class SelectGrammar extends TableGrammar {
    public SelectGrammar() {
        query = "SELECT ";
    }
}
