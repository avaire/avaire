package com.avairebot.contracts.database.grammar;

public abstract class CreateGrammar extends AlterGrammar {

    public CreateGrammar() {
        query = "CREATE TABLE ";
    }
}
