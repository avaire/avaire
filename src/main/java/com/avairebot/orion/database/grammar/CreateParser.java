package com.avairebot.orion.database.grammar;

import com.avairebot.orion.contracts.database.grammar.GrammarParser;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.schema.Blueprint;

import java.sql.SQLException;

public class CreateParser extends GrammarParser {
    @Override
    public String parse(DatabaseManager manager, Blueprint blueprint) throws SQLException {
        switch (ConnectionType.getType(manager.getConnection())) {
            case MySQL:
                return setupAndRun(new com.avairebot.orion.database.grammar.mysql.Create(), blueprint, manager, options);
            case SQLite:
                return setupAndRun(new com.avairebot.orion.database.grammar.sqlite.Create(), blueprint, manager, options);

            default:
                return null;
        }
    }
}
