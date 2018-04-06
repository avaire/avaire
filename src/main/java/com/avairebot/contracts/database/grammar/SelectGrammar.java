package com.avairebot.contracts.database.grammar;

public abstract class SelectGrammar extends TableGrammar {

    public SelectGrammar() {
        query = "SELECT ";
    }
}
