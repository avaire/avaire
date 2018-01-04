package com.avairebot.database.grammar;

import com.avairebot.contracts.database.grammar.GrammarParser;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.schema.Blueprint;

import java.sql.SQLException;

public class CreateParser extends GrammarParser {
    @Override
    public String parse(DatabaseManager manager, Blueprint blueprint) throws SQLException {
        switch (ConnectionType.getType(manager.getConnection())) {
            case MySQL:
                return setupAndRun(new com.avairebot.database.grammar.mysql.Create(), blueprint, manager, options);
            case SQLite:
                return setupAndRun(new com.avairebot.database.grammar.sqlite.Create(), blueprint, manager, options);

            default:
                return null;
        }
    }
}
