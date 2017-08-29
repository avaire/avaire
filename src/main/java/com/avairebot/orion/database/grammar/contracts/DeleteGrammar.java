package com.avairebot.orion.database.grammar.contracts;

import com.avairebot.orion.contracts.database.grammar.TableGrammar;

public abstract class DeleteGrammar extends TableGrammar {
    public DeleteGrammar() {
        query = "DELETE FROM ";
    }
}
