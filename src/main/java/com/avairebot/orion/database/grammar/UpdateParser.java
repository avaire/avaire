package com.avairebot.orion.database.grammar;

import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.query.QueryBuilder;

import java.sql.SQLException;

public class UpdateParser extends GrammarParser {
    @Override
    public String parse(DatabaseManager manager, QueryBuilder query) throws SQLException {
        switch (ConnectionType.getType(manager.getConnection())) {
            case MySQL:
                return setupAndRun(new com.avairebot.orion.database.grammar.mysql.Update(), query, manager, options);

            case SQLite:
                return setupAndRun(new com.avairebot.orion.database.grammar.sqlite.Update(), query, manager, options);

            default:
                return null;
        }
    }
}
