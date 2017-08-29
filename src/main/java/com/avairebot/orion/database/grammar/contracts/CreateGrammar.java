package com.avairebot.orion.database.grammar.contracts;

import com.avairebot.orion.contracts.database.grammar.AlterGrammar;

public abstract class CreateGrammar extends AlterGrammar {
    public CreateGrammar() {
        query = "CREATE TABLE ";
    }
}
