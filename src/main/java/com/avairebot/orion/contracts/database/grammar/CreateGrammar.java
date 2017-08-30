package com.avairebot.orion.contracts.database.grammar;

public abstract class CreateGrammar extends AlterGrammar {
    public CreateGrammar() {
        query = "CREATE TABLE ";
    }
}
