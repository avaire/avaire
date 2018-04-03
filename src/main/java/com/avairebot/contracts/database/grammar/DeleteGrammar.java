package com.avairebot.contracts.database.grammar;

public abstract class DeleteGrammar extends TableGrammar {

    public DeleteGrammar() {
        query = "DELETE FROM ";
    }
}
