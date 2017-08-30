package com.avairebot.orion.database.grammar;

import com.avairebot.orion.contracts.database.grammar.GrammarParser;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.query.QueryBuilder;

import java.sql.SQLException;

public class DeleteParser extends GrammarParser {
    @Override
    public String parse(DatabaseManager manager, QueryBuilder query) throws SQLException {
        switch (ConnectionType.getType(manager.getConnection())) {
            case MySQL:
                return setupAndRun(new com.avairebot.orion.database.grammar.mysql.Delete(), query, manager, options);

            case SQLite:
                return setupAndRun(new com.avairebot.orion.database.grammar.sqlite.Delete(), query, manager, options);

            default:
                return null;
        }
    }
}
