package com.avairebot.orion.contracts.database.grammar;

public abstract class SelectGrammar extends TableGrammar {
    public SelectGrammar() {
        query = "SELECT ";
    }
}
